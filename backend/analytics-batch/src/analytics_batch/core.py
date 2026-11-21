"""운영 DB → S3 → BigQuery 야간 배치의 핵심 흐름.

외부 시스템(MySQL, S3, BigQuery)은 아래 Protocol 뒤에 두고, 이 파일은 순서와 안전 규칙만 다룬다.

순서:
1. 이전 실행이 S3에 파일을 남기고 적재에 실패했다면, MySQL을 다시 읽지 않고 그 파일부터 적재한다.
2. 체크포인트(last_id)에서 겹침 구간만큼 뒤로 물러나 PK 순서로 조금씩 읽는다.
3. 읽은 행을 gzip NDJSON으로 S3에 둔다.
4. BigQuery raw에 적재하고 core에 MERGE한 뒤 행 수를 대조한다.
5. 4가 모두 성공했을 때만 체크포인트를 앞으로 옮긴다.

값이 바뀌거나 지워지는 작은 테이블은 증분 대신 전체 교체(snapshot)로 옮긴다.
매번 처음부터 끝까지 읽어 raw에 덧붙이고, 행 수가 맞으면 core를 그 실행 분량으로 통째로 바꾼다.
"""

from __future__ import annotations

import gzip
import io
import json
import time
import uuid
from dataclasses import dataclass, field
from datetime import date, datetime, timezone
from decimal import Decimal
from typing import Any, Callable, Protocol, Sequence


@dataclass(frozen=True)
class Column:
    name: str
    bq_type: str  # INT64, DATETIME, STRING ...


INCREMENTAL = "incremental"  # 추가만 되는 테이블: 마지막 id 이후만 읽어 core에 합친다
SNAPSHOT = "snapshot"  # 수정·삭제가 있는 작은 테이블: 매번 전체를 읽어 core를 교체한다


@dataclass(frozen=True)
class TableSpec:
    """가져올 테이블 하나의 정의. PK가 단조 증가하는 정수여야 한다.

    columns 에 적은 열만 읽는다. 개인정보나 본문처럼 분석에 필요 없는 열은 적지 않는다.
    """

    name: str
    key: str
    columns: tuple[Column, ...]
    partition_column: str | None = None  # core 테이블 파티션 기준(DATETIME 열)
    cluster_columns: tuple[str, ...] = ()
    mode: str = INCREMENTAL
    chunk_size: int | None = None  # 행이 큰 테이블은 Settings 값보다 작게 읽는다
    sleep_seconds: float | None = None
    max_rows_per_run: int | None = None  # Settings 값보다 작을 때만 적용된다

    @property
    def column_names(self) -> list[str]:
        return [column.name for column in self.columns]


ARTICLE_READ_HISTORY = TableSpec(
    name="article_read_history",
    key="id",
    columns=(
        Column("id", "INT64"),
        Column("member_id", "INT64"),
        Column("article_id", "INT64"),
        Column("newsletter_id", "INT64"),
        Column("category_id", "INT64"),
        Column("read_at", "DATETIME"),
        Column("created_at", "DATETIME"),
        Column("updated_at", "DATETIME"),
    ),
    partition_column="read_at",
    cluster_columns=("member_id",),
)

# 본문(contents, contents_text)과 제목은 가져오지 않는다. is_read 는 나중에 바뀌는 값이라
# 증분으로는 맞출 수 없으므로 뺐다. 읽음 여부는 article_read_history 로 본다.
# 행이 커서(본문 일부가 같은 페이지에 있다) 한 번에 적게 읽고 더 오래 쉰다.
# 운영 RDS는 여유 메모리가 100MiB 안팎이다. 10만 행을 이어서 읽었을 때 여유 메모리가 45MiB 줄었으므로
# 한 번 실행에 2만 행까지만 읽는다. 하루 증가분은 이보다 적다.
ARTICLE = TableSpec(
    name="article",
    key="id",
    columns=(
        Column("id", "INT64"),
        Column("member_id", "INT64"),
        Column("newsletter_id", "INT64"),
        Column("arrived_date_time", "DATETIME"),
        Column("expected_read_time", "INT64"),
        Column("created_at", "DATETIME"),
    ),
    partition_column="arrived_date_time",
    cluster_columns=("member_id",),
    chunk_size=500,
    sleep_seconds=0.3,
    max_rows_per_run=20_000,
)

# 이메일, 닉네임, 생년월일, 성별, 프로필 이미지는 가져오지 않는다.
MEMBER = TableSpec(
    name="member",
    key="id",
    columns=(
        Column("id", "INT64"),
        Column("provider", "STRING"),
        Column("created_at", "DATETIME"),
    ),
    mode=SNAPSHOT,
)

SUBSCRIBE = TableSpec(
    name="subscribe",
    key="id",
    columns=(
        Column("id", "INT64"),
        Column("member_id", "INT64"),
        Column("newsletter_id", "INT64"),
        Column("status", "STRING"),
        Column("created_at", "DATETIME"),
        Column("updated_at", "DATETIME"),
    ),
    cluster_columns=("member_id",),
    mode=SNAPSHOT,
)

NEWSLETTER = TableSpec(
    name="newsletter",
    key="id",
    columns=(
        Column("id", "INT64"),
        Column("name", "STRING"),
        Column("category_id", "INT64"),
        Column("status", "STRING"),
        Column("suspended_at", "DATE"),
        Column("source", "STRING"),
        Column("created_at", "DATETIME"),
    ),
    mode=SNAPSHOT,
)

CATEGORY = TableSpec(
    name="category",
    key="id",
    columns=(Column("id", "INT64"), Column("name", "STRING")),
    mode=SNAPSHOT,
)

# 이메일, 성별, 연령대는 가져오지 않는다.
WITHDRAWN_MEMBER = TableSpec(
    name="withdrawn_member",
    key="id",
    columns=(
        Column("id", "INT64"),
        Column("member_id", "INT64"),
        Column("provider", "STRING"),
        Column("joined_date", "DATE"),
        Column("deleted_date", "DATE"),
        Column("last_read_date", "DATE"),
        Column("continue_reading", "INT64"),
        Column("bookmarked_count", "INT64"),
        Column("highlight_count", "INT64"),
        Column("total_read_count", "INT64"),
        Column("subscribe_count", "INT64"),
        Column("challenge_count", "INT64"),
        Column("badge_count", "INT64"),
        Column("created_at", "DATETIME"),
    ),
    mode=SNAPSHOT,
)

TABLES = {
    spec.name: spec
    for spec in (ARTICLE_READ_HISTORY, ARTICLE, MEMBER, SUBSCRIBE, NEWSLETTER, CATEGORY, WITHDRAWN_MEMBER)
}


@dataclass(frozen=True)
class Settings:
    chunk_size: int = 2000  # 한 번의 SELECT로 읽는 행 수
    overlap_rows: int = 1000  # 늦게 커밋된 행을 잡으려고 다시 읽는 id 폭
    sleep_seconds: float = 0.2  # 묶음 사이에 쉬는 시간
    max_rows_per_run: int = 200_000  # 한 번 실행에서 읽는 행 상한
    time_budget_seconds: float = 480.0  # 추출에 쓰는 시간 상한
    part_rows: int = 50_000  # S3 파일 하나에 담는 행 수


class Source(Protocol):
    def fetch(self, spec: TableSpec, after_id: int, limit: int) -> list[dict[str, Any]]:
        """key > after_id 인 행을 key 오름차순으로 최대 limit개 돌려준다."""


class Store(Protocol):
    def get_json(self, key: str) -> dict[str, Any] | None: ...

    def put_json(self, key: str, value: dict[str, Any]) -> None: ...

    def delete(self, key: str) -> None: ...

    def put_bytes(self, key: str, data: bytes) -> None: ...

    def get_bytes(self, key: str) -> bytes: ...


class Sink(Protocol):
    def load_part(self, spec: TableSpec, job_id: str, data: bytes) -> None:
        """gzip NDJSON 한 파일을 raw 테이블에 덧붙인다. 같은 job_id는 두 번 적재되지 않아야 한다."""

    def count_run(self, spec: TableSpec, run_id: str) -> int:
        """raw 테이블에서 해당 실행이 적재한 행 수."""

    def merge_run(self, spec: TableSpec, run_id: str) -> None:
        """raw의 해당 실행 분량을 core에 key 기준으로 합친다."""

    def replace_run(self, spec: TableSpec, run_id: str) -> None:
        """core를 raw의 해당 실행 분량으로 통째로 바꾼다."""


class RowCountMismatch(RuntimeError):
    pass


class SnapshotIncomplete(RuntimeError):
    """전체 교체 테이블을 끝까지 읽지 못했다. 일부만으로 core를 바꾸면 안 된다."""


@dataclass
class RunResult:
    table: str
    run_id: str | None = None
    recovered_run_id: str | None = None
    rows: int = 0
    parts: int = 0
    last_id_before: int = 0
    last_id_after: int = 0
    stopped_early: bool = False
    stop_reason: str | None = None

    def as_dict(self) -> dict[str, Any]:
        return dict(self.__dict__)


def checkpoint_key(spec: TableSpec) -> str:
    return f"state/{spec.name}.json"


def pending_key(spec: TableSpec) -> str:
    return f"state/{spec.name}.pending.json"


def new_run_id(now: datetime) -> str:
    return f"{now.strftime('%Y%m%dT%H%M%SZ')}-{uuid.uuid4().hex[:8]}"


def _json_default(value: Any) -> Any:
    if isinstance(value, datetime):
        return value.isoformat(sep="T", timespec="microseconds")
    if isinstance(value, date):
        return value.isoformat()
    if isinstance(value, Decimal):
        return str(value)
    if isinstance(value, (bytes, bytearray)):
        return value.hex()
    raise TypeError(f"직렬화할 수 없는 값: {type(value)!r}")


def encode_part(rows: Sequence[dict[str, Any]], run_id: str, extracted_at: datetime) -> bytes:
    """행 목록을 배치 정보(_run_id, _extracted_at)와 함께 gzip NDJSON으로 만든다."""
    stamp = extracted_at.astimezone(timezone.utc).strftime("%Y-%m-%d %H:%M:%S.%f UTC")
    buffer = io.BytesIO()
    # mtime=0: 같은 입력이면 같은 바이트가 나오도록 한다.
    with gzip.GzipFile(fileobj=buffer, mode="wb", mtime=0) as gz:
        for row in rows:
            line = dict(row)
            line["_run_id"] = run_id
            line["_extracted_at"] = stamp
            gz.write(json.dumps(line, default=_json_default, ensure_ascii=False).encode("utf-8"))
            gz.write(b"\n")
    return buffer.getvalue()


def _job_id(spec: TableSpec, run_id: str, index: int) -> str:
    return f"load_{spec.name}_{run_id}_{index:04d}".replace("-", "_")


def _load_and_commit(spec: TableSpec, store: Store, sink: Sink, pending: dict[str, Any]) -> None:
    """S3에 있는 파일을 적재·병합·대조하고, 성공하면 체크포인트를 옮긴 뒤 pending을 지운다."""
    run_id = pending["run_id"]
    for index, key in enumerate(pending["parts"]):
        sink.load_part(spec, _job_id(spec, run_id, index), store.get_bytes(key))

    loaded = sink.count_run(spec, run_id)
    if loaded != pending["rows"]:
        raise RowCountMismatch(
            f"{spec.name} run {run_id}: 추출 {pending['rows']}행, 적재 {loaded}행"
        )

    sink.merge_run(spec, run_id)

    current = store.get_json(checkpoint_key(spec)) or {}
    store.put_json(
        checkpoint_key(spec),
        {
            "last_id": max(int(current.get("last_id", 0)), int(pending["max_id"])),
            "run_id": run_id,
            "rows": pending["rows"],
            "updated_at": pending["extracted_at"],
        },
    )
    store.delete(pending_key(spec))


@dataclass
class _Extract:
    run_id: str
    extracted_at: datetime
    part_keys: list[str]
    rows: int
    max_id: int
    stop_reason: str | None


def _extract(
    spec: TableSpec,
    source: Source,
    store: Store,
    settings: Settings,
    start_after: int,
    now: Callable[[], datetime],
    monotonic: Callable[[], float],
    sleep: Callable[[float], None],
) -> _Extract:
    """start_after 다음 행부터 PK 순서로 조금씩 읽어 S3에 둔다."""
    chunk_size = spec.chunk_size or settings.chunk_size
    sleep_seconds = settings.sleep_seconds if spec.sleep_seconds is None else spec.sleep_seconds
    max_rows = min(settings.max_rows_per_run, spec.max_rows_per_run or settings.max_rows_per_run)

    started = monotonic()
    extracted_at = now()
    run_id = new_run_id(extracted_at)
    prefix = f"raw/{spec.name}/dt={extracted_at.strftime('%Y-%m-%d')}/run_id={run_id}"

    cursor = start_after
    total = 0
    stop_reason: str | None = None
    part_keys: list[str] = []
    buffer: list[dict[str, Any]] = []

    def flush() -> None:
        if not buffer:
            return
        key = f"{prefix}/part-{len(part_keys):04d}.ndjson.gz"
        store.put_bytes(key, encode_part(buffer, run_id, extracted_at))
        part_keys.append(key)
        buffer.clear()

    while True:
        rows = source.fetch(spec, cursor, chunk_size)
        if not rows:
            break
        buffer.extend(rows)
        total += len(rows)
        cursor = int(rows[-1][spec.key])
        if len(buffer) >= settings.part_rows:
            flush()
        if len(rows) < chunk_size:
            break
        if total >= max_rows:
            stop_reason = "max_rows_per_run"
            break
        if monotonic() - started >= settings.time_budget_seconds:
            stop_reason = "time_budget"
            break
        sleep(sleep_seconds)
    flush()

    return _Extract(run_id, extracted_at, part_keys, total, cursor, stop_reason)


def run(
    spec: TableSpec,
    source: Source,
    store: Store,
    sink: Sink,
    settings: Settings = Settings(),
    *,
    now: Callable[[], datetime] = lambda: datetime.now(timezone.utc),
    monotonic: Callable[[], float] = time.monotonic,
    sleep: Callable[[float], None] = time.sleep,
) -> RunResult:
    if spec.mode == SNAPSHOT:
        return _run_snapshot(spec, source, store, sink, settings, now, monotonic, sleep)
    return _run_incremental(spec, source, store, sink, settings, now, monotonic, sleep)


def _run_incremental(
    spec: TableSpec,
    source: Source,
    store: Store,
    sink: Sink,
    settings: Settings,
    now: Callable[[], datetime],
    monotonic: Callable[[], float],
    sleep: Callable[[float], None],
) -> RunResult:
    result = RunResult(table=spec.name)

    # 1. 이전 실행이 남긴 파일부터 처리한다. MySQL은 다시 읽지 않는다.
    pending = store.get_json(pending_key(spec))
    if pending is not None:
        _load_and_commit(spec, store, sink, pending)
        result.recovered_run_id = pending["run_id"]

    checkpoint = store.get_json(checkpoint_key(spec)) or {}
    last_id = int(checkpoint.get("last_id", 0))
    result.last_id_before = last_id
    result.last_id_after = last_id

    # 2~3. PK 순서로 조금씩 읽어 S3에 둔다.
    extract = _extract(
        spec, source, store, settings, max(0, last_id - settings.overlap_rows), now, monotonic, sleep
    )
    if extract.stop_reason is not None:
        result.stopped_early, result.stop_reason = True, extract.stop_reason

    if extract.rows == 0:
        return result

    # 적재 전에 무엇을 올릴지 S3에 적어 둔다. 여기서부터 실패하면 다음 실행이 1번에서 이어받는다.
    pending = {
        "run_id": extract.run_id,
        "parts": extract.part_keys,
        "rows": extract.rows,
        "max_id": extract.max_id,
        "extracted_at": extract.extracted_at.isoformat(),
    }
    store.put_json(pending_key(spec), pending)

    # 4~5. 적재·병합·대조가 모두 끝나야 체크포인트가 움직인다.
    _load_and_commit(spec, store, sink, pending)

    result.run_id = extract.run_id
    result.rows = extract.rows
    result.parts = len(extract.part_keys)
    result.last_id_after = max(last_id, extract.max_id)
    return result


def _run_snapshot(
    spec: TableSpec,
    source: Source,
    store: Store,
    sink: Sink,
    settings: Settings,
    now: Callable[[], datetime],
    monotonic: Callable[[], float],
    sleep: Callable[[float], None],
) -> RunResult:
    """테이블 전체를 읽어 core를 교체한다. 실패하면 core는 이전 상태로 남고 다음 실행이 처음부터 다시 한다."""
    result = RunResult(table=spec.name)

    extract = _extract(spec, source, store, settings, 0, now, monotonic, sleep)
    if extract.stop_reason is not None:
        raise SnapshotIncomplete(
            f"{spec.name}: {extract.rows}행을 읽고 {extract.stop_reason} 상한에 걸렸습니다. "
            "전체 교체로 옮기기에는 큰 테이블입니다"
        )

    # 원본이 비어 있으면 아무것도 바꾸지 않는다. 접속 대상이 잘못됐을 때 core를 비우지 않기 위해서다.
    if extract.rows == 0:
        return result

    for index, key in enumerate(extract.part_keys):
        sink.load_part(spec, _job_id(spec, extract.run_id, index), store.get_bytes(key))

    loaded = sink.count_run(spec, extract.run_id)
    if loaded != extract.rows:
        raise RowCountMismatch(
            f"{spec.name} run {extract.run_id}: 추출 {extract.rows}행, 적재 {loaded}행"
        )

    sink.replace_run(spec, extract.run_id)

    store.put_json(
        checkpoint_key(spec),
        {
            "last_id": extract.max_id,
            "run_id": extract.run_id,
            "rows": extract.rows,
            "updated_at": extract.extracted_at.isoformat(),
        },
    )

    result.run_id = extract.run_id
    result.rows = extract.rows
    result.parts = len(extract.part_keys)
    result.last_id_after = extract.max_id
    return result
