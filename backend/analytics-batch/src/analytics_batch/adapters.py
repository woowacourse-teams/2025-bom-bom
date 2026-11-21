"""MySQL, S3, BigQuery에 실제로 붙는 구현. 외부 라이브러리는 여기서만 쓴다."""

from __future__ import annotations

import json
import time
from typing import Any

from .core import TableSpec

BIGQUERY_API = "https://bigquery.googleapis.com/bigquery/v2"
BIGQUERY_UPLOAD_API = "https://bigquery.googleapis.com/upload/bigquery/v2"
BIGQUERY_SCOPES = ["https://www.googleapis.com/auth/bigquery"]

BATCH_COLUMNS = (("_run_id", "STRING"), ("_extracted_at", "TIMESTAMP"))


class MySqlSource:
    """PK 범위로만 읽는다. 묶음마다 짧은 자동 커밋 조회 하나를 쓴다."""

    def __init__(
        self,
        *,
        host: str,
        port: int,
        database: str,
        user: str,
        password: str,
        max_execution_ms: int = 5000,
        ssl_ca: str | None = None,
    ) -> None:
        import ssl

        import pymysql
        import pymysql.cursors

        # MySQL 8.4 기본 인증(caching_sha2_password)은 암호화 연결이 필요하다. 항상 TLS로 붙는다.
        # ssl_ca 를 주면 서버 인증서까지 검증하고, 없으면 암호화만 한다.
        if ssl_ca:
            tls = ssl.create_default_context(cafile=ssl_ca)
        else:
            tls = ssl.create_default_context()
            tls.check_hostname = False
            tls.verify_mode = ssl.CERT_NONE

        self._max_execution_ms = max_execution_ms
        self._connection = pymysql.connect(
            host=host,
            port=port,
            user=user,
            password=password,
            database=database,
            charset="utf8mb4",
            autocommit=True,
            ssl=tls,
            connect_timeout=5,
            read_timeout=30,
            write_timeout=30,
            cursorclass=pymysql.cursors.DictCursor,
        )

    def fetch(self, spec: TableSpec, after_id: int, limit: int) -> list[dict[str, Any]]:
        columns = ", ".join(f"`{name}`" for name in spec.column_names)
        # 테이블·열 이름은 코드에 고정된 TableSpec에서만 오고, 값은 바인딩한다.
        sql = (
            f"SELECT /*+ MAX_EXECUTION_TIME({self._max_execution_ms}) */ {columns} "
            f"FROM `{spec.name}` WHERE `{spec.key}` > %s ORDER BY `{spec.key}` LIMIT %s"
        )
        with self._connection.cursor() as cursor:
            cursor.execute(sql, (after_id, limit))
            return list(cursor.fetchall())

    def close(self) -> None:
        self._connection.close()


class S3Store:
    def __init__(self, bucket: str, client: Any = None) -> None:
        if client is None:
            import boto3

            client = boto3.client("s3")
        self._bucket = bucket
        self._client = client

    def get_json(self, key: str) -> dict[str, Any] | None:
        try:
            body = self._client.get_object(Bucket=self._bucket, Key=key)["Body"].read()
        except self._client.exceptions.NoSuchKey:
            return None
        return json.loads(body)

    def put_json(self, key: str, value: dict[str, Any]) -> None:
        self._client.put_object(
            Bucket=self._bucket,
            Key=key,
            Body=json.dumps(value, ensure_ascii=False).encode("utf-8"),
            ContentType="application/json",
        )

    def delete(self, key: str) -> None:
        self._client.delete_object(Bucket=self._bucket, Key=key)

    def put_bytes(self, key: str, data: bytes) -> None:
        self._client.put_object(Bucket=self._bucket, Key=key, Body=data)

    def get_bytes(self, key: str) -> bytes:
        return self._client.get_object(Bucket=self._bucket, Key=key)["Body"].read()


class BigQueryError(RuntimeError):
    pass


class BigQuerySink:
    """BigQuery REST API를 직접 호출한다.

    인증은 Application Default Credentials를 쓴다. Lambda에서는
    GOOGLE_APPLICATION_CREDENTIALS가 가리키는 Workload Identity Federation 설정 파일로
    AWS 역할을 Google 서비스 계정으로 바꾼다. 서비스 계정 키 파일은 쓰지 않는다.
    """

    def __init__(
        self,
        *,
        project: str,
        location: str,
        raw_dataset: str = "raw",
        core_dataset: str = "core",
        session: Any = None,
        poll_seconds: float = 2.0,
        timeout_seconds: float = 300.0,
    ) -> None:
        if session is None:
            import google.auth
            from google.auth.transport.requests import AuthorizedSession

            credentials, _ = google.auth.default(scopes=BIGQUERY_SCOPES)
            session = AuthorizedSession(credentials)
        self._session = session
        self._project = project
        self._location = location
        self._raw = raw_dataset
        self._core = core_dataset
        self._poll_seconds = poll_seconds
        self._timeout_seconds = timeout_seconds

    # --- Sink 구현 ---

    def load_part(self, spec: TableSpec, job_id: str, data: bytes) -> None:
        fields = [{"name": c.name, "type": c.bq_type, "mode": "NULLABLE"} for c in spec.columns]
        fields += [{"name": name, "type": bq_type, "mode": "NULLABLE"} for name, bq_type in BATCH_COLUMNS]
        job = {
            "jobReference": {"projectId": self._project, "jobId": job_id, "location": self._location},
            "configuration": {
                "load": {
                    "destinationTable": {
                        "projectId": self._project,
                        "datasetId": self._raw,
                        "tableId": spec.name,
                    },
                    "sourceFormat": "NEWLINE_DELIMITED_JSON",
                    "schema": {"fields": fields},
                    "writeDisposition": "WRITE_APPEND",
                    "createDisposition": "CREATE_IF_NEEDED",
                    "timePartitioning": {"type": "DAY", "field": "_extracted_at"},
                }
            },
        }
        boundary = "analytics-batch-boundary"
        body = (
            f"--{boundary}\r\nContent-Type: application/json; charset=UTF-8\r\n\r\n".encode()
            + json.dumps(job).encode("utf-8")
            + f"\r\n--{boundary}\r\nContent-Type: application/octet-stream\r\n\r\n".encode()
            + data
            + f"\r\n--{boundary}--\r\n".encode()
        )
        response = self._session.post(
            f"{BIGQUERY_UPLOAD_API}/projects/{self._project}/jobs?uploadType=multipart",
            data=body,
            headers={"Content-Type": f"multipart/related; boundary={boundary}"},
            timeout=120,
        )
        # 409: 같은 job_id가 이미 있다. 재시도 상황이므로 새로 만들지 않고 그 작업의 결과를 따른다.
        if response.status_code != 409:
            self._raise_for_status(response, "load job 생성")
        self._wait(job_id)

    def count_run(self, spec: TableSpec, run_id: str) -> int:
        rows = self._query(
            f"SELECT COUNT(*) AS n FROM `{self._project}.{self._raw}.{spec.name}` WHERE _run_id = @run_id",
            run_id,
        )
        return int(rows[0]["f"][0]["v"])

    def merge_run(self, spec: TableSpec, run_id: str) -> None:
        self._query(self.core_ddl(spec), None)
        self._query(self.merge_sql(spec), run_id)

    def replace_run(self, spec: TableSpec, run_id: str) -> None:
        self._query(self.replace_sql(spec), run_id)

    # --- SQL ---

    def core_ddl(self, spec: TableSpec) -> str:
        columns = ",\n  ".join(f"{c.name} {c.bq_type}" for c in spec.columns)
        batch = ",\n  ".join(f"{name} {bq_type}" for name, bq_type in BATCH_COLUMNS)
        return (
            f"CREATE TABLE IF NOT EXISTS `{self._project}.{self._core}.{spec.name}` (\n  {columns},\n  {batch}\n)"
            f"{self._layout(spec)}"
        )

    def replace_sql(self, spec: TableSpec) -> str:
        """core를 한 실행 분량으로 통째로 바꾼다. 한 문장이라 중간 상태가 보이지 않는다."""
        names = ", ".join(spec.column_names + [name for name, _ in BATCH_COLUMNS])
        return (
            f"CREATE OR REPLACE TABLE `{self._project}.{self._core}.{spec.name}`"
            f"{self._layout(spec)}\nAS\n"
            f"SELECT {names} FROM `{self._project}.{self._raw}.{spec.name}`\n"
            f"WHERE _run_id = @run_id"
        )

    @staticmethod
    def _layout(spec: TableSpec) -> str:
        partition = f"\nPARTITION BY DATE({spec.partition_column})" if spec.partition_column else ""
        cluster = f"\nCLUSTER BY {', '.join(spec.cluster_columns)}" if spec.cluster_columns else ""
        return f"{partition}{cluster}"

    def merge_sql(self, spec: TableSpec) -> str:
        names = spec.column_names + [name for name, _ in BATCH_COLUMNS]
        updates = ", ".join(f"{name} = s.{name}" for name in names if name != spec.key)
        inserts = ", ".join(names)
        values = ", ".join(f"s.{name}" for name in names)
        return (
            f"MERGE `{self._project}.{self._core}.{spec.name}` t\n"
            f"USING (\n"
            f"  SELECT * FROM `{self._project}.{self._raw}.{spec.name}`\n"
            f"  WHERE _run_id = @run_id\n"
            f"  QUALIFY ROW_NUMBER() OVER (PARTITION BY {spec.key} ORDER BY _extracted_at DESC) = 1\n"
            f") s\n"
            f"ON t.{spec.key} = s.{spec.key}\n"
            f"WHEN MATCHED THEN UPDATE SET {updates}\n"
            f"WHEN NOT MATCHED THEN INSERT ({inserts}) VALUES ({values})"
        )

    # --- REST 호출 ---

    def _query(self, sql: str, run_id: str | None) -> list[dict[str, Any]]:
        body: dict[str, Any] = {
            "query": sql,
            "useLegacySql": False,
            "location": self._location,
            "timeoutMs": 10000,
        }
        if run_id is not None:
            body["parameterMode"] = "NAMED"
            body["queryParameters"] = [
                {
                    "name": "run_id",
                    "parameterType": {"type": "STRING"},
                    "parameterValue": {"value": run_id},
                }
            ]
        response = self._session.post(
            f"{BIGQUERY_API}/projects/{self._project}/queries", json=body, timeout=60
        )
        self._raise_for_status(response, "query")
        payload = response.json()
        job_id = payload["jobReference"]["jobId"]
        deadline = time.monotonic() + self._timeout_seconds
        while not payload.get("jobComplete"):
            if time.monotonic() > deadline:
                raise BigQueryError(f"query {job_id} 가 제한 시간 안에 끝나지 않았습니다")
            response = self._session.get(
                f"{BIGQUERY_API}/projects/{self._project}/queries/{job_id}",
                params={"location": self._location, "timeoutMs": 10000},
                timeout=60,
            )
            self._raise_for_status(response, "query 결과 조회")
            payload = response.json()
        if payload.get("errors"):
            raise BigQueryError(f"query {job_id} 실패: {payload['errors']}")
        return payload.get("rows", [])

    def _wait(self, job_id: str) -> None:
        deadline = time.monotonic() + self._timeout_seconds
        while True:
            response = self._session.get(
                f"{BIGQUERY_API}/projects/{self._project}/jobs/{job_id}",
                params={"location": self._location},
                timeout=60,
            )
            self._raise_for_status(response, "job 상태 조회")
            status = response.json().get("status", {})
            if status.get("state") == "DONE":
                if status.get("errorResult"):
                    raise BigQueryError(f"job {job_id} 실패: {status['errorResult']}")
                return
            if time.monotonic() > deadline:
                raise BigQueryError(f"job {job_id} 가 제한 시간 안에 끝나지 않았습니다")
            time.sleep(self._poll_seconds)

    @staticmethod
    def _raise_for_status(response: Any, what: str) -> None:
        if response.status_code >= 400:
            raise BigQueryError(f"{what} 실패: HTTP {response.status_code} {response.text[:500]}")
