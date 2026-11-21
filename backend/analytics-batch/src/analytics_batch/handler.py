"""Lambda 진입점. 환경 변수로 설정을 읽고 테이블별로 core.run 을 실행한다."""

from __future__ import annotations

import json
import logging
import os
from typing import Any

from . import core

logger = logging.getLogger()
logger.setLevel(logging.INFO)


def _env(name: str, default: str | None = None) -> str:
    value = os.environ.get(name, default)
    if value is None or value == "":
        raise RuntimeError(f"환경 변수 {name} 가 필요합니다")
    return value


def settings_from_env(event: dict[str, Any] | None = None) -> core.Settings:
    """환경 변수로 설정을 읽는다. 수동 실행 때는 event 의 max_rows_per_run 으로 행 상한만 낮출 수 있다."""
    defaults = core.Settings()
    max_rows = (event or {}).get("max_rows_per_run") or os.environ.get(
        "MAX_ROWS_PER_RUN", defaults.max_rows_per_run
    )
    return core.Settings(
        chunk_size=int(os.environ.get("CHUNK_SIZE", defaults.chunk_size)),
        overlap_rows=int(os.environ.get("OVERLAP_ROWS", defaults.overlap_rows)),
        sleep_seconds=float(os.environ.get("SLEEP_SECONDS", defaults.sleep_seconds)),
        max_rows_per_run=int(max_rows),
        time_budget_seconds=float(os.environ.get("TIME_BUDGET_SECONDS", defaults.time_budget_seconds)),
        part_rows=int(os.environ.get("PART_ROWS", defaults.part_rows)),
    )


def _db_password() -> str:
    import boto3

    parameter = boto3.client("ssm").get_parameter(
        Name=_env("DB_PASSWORD_PARAMETER"), WithDecryption=True
    )
    return parameter["Parameter"]["Value"]


def handler(event: dict[str, Any] | None, context: Any) -> dict[str, Any]:
    from .adapters import BigQuerySink, MySqlSource, S3Store

    event = event or {}
    table_names = event.get("tables") or os.environ.get("TABLES", "article_read_history").split(",")
    settings = settings_from_env(event)

    store = S3Store(_env("S3_BUCKET"))
    sink = BigQuerySink(
        project=_env("GCP_PROJECT", "bombom-analytics"),
        location=_env("BIGQUERY_LOCATION", "asia-northeast3"),
        raw_dataset=_env("BIGQUERY_RAW_DATASET", "raw"),
        core_dataset=_env("BIGQUERY_CORE_DATASET", "core"),
    )
    source = MySqlSource(
        host=_env("DB_HOST"),
        port=int(_env("DB_PORT", "3306")),
        database=_env("DB_NAME"),
        user=_env("DB_USER"),
        password=_db_password(),
        ssl_ca=os.environ.get("DB_SSL_CA") or None,
    )

    # 테이블 하나가 실패해도 나머지는 계속 옮긴다. 실패가 하나라도 있으면 마지막에 예외를 던져
    # Lambda 오류로 집계되게 한다(알람이 이 지표를 본다).
    specs = [core.TABLES[name.strip()] for name in table_names]
    results = []
    failures = []
    try:
        for spec in specs:
            try:
                result = core.run(spec, source, store, sink, settings)
            except Exception as error:  # noqa: BLE001 - 다음 테이블로 넘어가려고 넓게 잡는다
                logger.exception("batch failed %s", spec.name)
                failures.append(f"{spec.name}: {type(error).__name__}: {error}")
                continue
            logger.info("batch result %s", json.dumps(result.as_dict(), ensure_ascii=False))
            results.append(result.as_dict())
    finally:
        source.close()
    if failures:
        raise RuntimeError(f"{len(failures)}개 테이블 실패 (성공 {len(results)}개): " + " | ".join(failures))
    return {"results": results}
