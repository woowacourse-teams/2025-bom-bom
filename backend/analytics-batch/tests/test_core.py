import gzip
import json
from datetime import datetime, timezone

import pytest

from analytics_batch import core
from analytics_batch.core import ARTICLE_READ_HISTORY as SPEC


class FakeSource:
    def __init__(self, rows):
        self.rows = sorted(rows, key=lambda row: row["id"])
        self.calls = []

    def fetch(self, spec, after_id, limit):
        self.calls.append((after_id, limit))
        return [row for row in self.rows if row["id"] > after_id][:limit]


class FakeStore:
    def __init__(self):
        self.objects = {}

    def get_json(self, key):
        return json.loads(self.objects[key]) if key in self.objects else None

    def put_json(self, key, value):
        self.objects[key] = json.dumps(value).encode()

    def delete(self, key):
        self.objects.pop(key, None)

    def put_bytes(self, key, data):
        self.objects[key] = data

    def get_bytes(self, key):
        return self.objects[key]


class FakeSink:
    """raw는 덧붙이기, core는 id 기준 병합. 같은 job_id는 한 번만 적재한다."""

    def __init__(self):
        self.raw = []
        self.core = {}
        self.jobs = set()
        self.fail_load = False
        self.fail_merge = False

    def load_part(self, spec, job_id, data):
        if self.fail_load:
            raise RuntimeError("bigquery down")
        if job_id in self.jobs:
            return
        self.jobs.add(job_id)
        for line in gzip.decompress(data).splitlines():
            self.raw.append(json.loads(line))

    def count_run(self, spec, run_id):
        return sum(1 for row in self.raw if row["_run_id"] == run_id)

    def merge_run(self, spec, run_id):
        if self.fail_merge:
            raise RuntimeError("merge failed")
        for row in self.raw:
            if row["_run_id"] == run_id:
                self.core[row["id"]] = row

    def replace_run(self, spec, run_id):
        if self.fail_merge:
            raise RuntimeError("replace failed")
        self.core = {row["id"]: row for row in self.raw if row["_run_id"] == run_id}


def make_rows(first, last):
    return [
        {
            "id": i,
            "member_id": i % 7,
            "article_id": 1000 + i,
            "newsletter_id": 3,
            "category_id": 2,
            "read_at": datetime(2026, 11, 19, 8, 30, 0, 123456),
            "created_at": datetime(2026, 11, 19, 8, 30, 0, 123456),
            "updated_at": datetime(2026, 11, 19, 8, 30, 0, 123456),
        }
        for i in range(first, last + 1)
    ]


class Clock:
    def __init__(self, step=0.0):
        self.value = 0.0
        self.step = step

    def monotonic(self):
        self.value += self.step
        return self.value


def run(source, store, sink, settings, clock=None, spec=SPEC):
    clock = clock or Clock()
    return core.run(
        spec,
        source,
        store,
        sink,
        settings,
        now=lambda: datetime(2026, 11, 19, 16, 0, 0, tzinfo=timezone.utc),
        monotonic=clock.monotonic,
        sleep=lambda seconds: None,
    )


SMALL = core.Settings(chunk_size=10, overlap_rows=5, sleep_seconds=0, part_rows=25)


def test_first_run_from_empty_checkpoint_copies_everything():
    source, store, sink = FakeSource(make_rows(1, 57)), FakeStore(), FakeSink()

    result = run(source, store, sink, SMALL)

    assert result.rows == 57 and result.last_id_after == 57 and not result.stopped_early
    assert sorted(sink.core) == list(range(1, 58))
    assert store.get_json("state/article_read_history.json")["last_id"] == 57
    assert store.get_json("state/article_read_history.pending.json") is None
    # 묶음 10행씩 쌓다가 25행을 넘는 순간 파일로 내보낸다: 30행 + 27행
    assert result.parts == 2
    assert all(call[1] == 10 for call in source.calls)


def test_staged_row_keeps_values_and_batch_columns():
    source, store, sink = FakeSource(make_rows(1, 1)), FakeStore(), FakeSink()

    result = run(source, store, sink, SMALL)

    row = sink.raw[0]
    assert row["read_at"] == "2026-11-19T08:30:00.123456"
    assert row["_run_id"] == result.run_id
    assert row["_extracted_at"] == "2026-11-19 16:00:00.000000 UTC"
    key = next(k for k in store.objects if k.endswith(".ndjson.gz"))
    assert key.startswith("raw/article_read_history/dt=2026-11-19/run_id=")


def test_overlap_rereads_late_rows_without_duplicates_in_core():
    rows = [row for row in make_rows(1, 30) if row["id"] != 28]  # 28번은 아직 커밋 전
    source, store, sink = FakeSource(rows), FakeStore(), FakeSink()
    run(source, store, sink, SMALL)
    assert 28 not in sink.core

    source.rows = make_rows(1, 33)  # 28번이 늦게 커밋되고 31~33이 새로 생김
    result = run(source, store, sink, SMALL)

    assert source.calls[-2][0] == 25 or (25, 10) in source.calls  # 30 - 겹침 5 = 25부터 다시 읽음
    assert sorted(sink.core) == list(range(1, 34))
    assert result.rows == 8  # 26~33
    assert len(sink.raw) == 29 + 8  # raw에는 겹친 행이 중복으로 남고 core에서 하나로 합쳐진다


def test_no_new_rows_does_nothing_when_overlap_is_zero():
    source, store, sink = FakeSource(make_rows(1, 12)), FakeStore(), FakeSink()
    settings = core.Settings(chunk_size=10, overlap_rows=0, sleep_seconds=0)
    run(source, store, sink, settings)
    raw_before = len(sink.raw)

    result = run(source, store, sink, settings)

    assert result.rows == 0 and result.run_id is None
    assert len(sink.raw) == raw_before


def test_stops_on_time_budget_and_next_run_resumes():
    source, store, sink = FakeSource(make_rows(1, 100)), FakeStore(), FakeSink()
    settings = core.Settings(chunk_size=10, overlap_rows=0, sleep_seconds=0, time_budget_seconds=3)

    first = run(source, store, sink, settings, Clock(step=1.0))

    assert first.stopped_early and first.stop_reason == "time_budget"
    assert 0 < first.last_id_after < 100
    assert store.get_json("state/article_read_history.json")["last_id"] == first.last_id_after
    assert sorted(sink.core) == list(range(1, first.last_id_after + 1))

    while store.get_json("state/article_read_history.json")["last_id"] < 100:
        run(source, store, sink, settings, Clock(step=1.0))
    assert sorted(sink.core) == list(range(1, 101))


def test_stops_on_row_cap():
    source, store, sink = FakeSource(make_rows(1, 100)), FakeStore(), FakeSink()
    settings = core.Settings(chunk_size=10, overlap_rows=0, sleep_seconds=0, max_rows_per_run=30)

    result = run(source, store, sink, settings)

    assert result.stop_reason == "max_rows_per_run" and result.rows == 30 and result.last_id_after == 30


def test_bigquery_failure_keeps_checkpoint_and_retry_reuses_staged_files():
    source, store, sink = FakeSource(make_rows(1, 40)), FakeStore(), FakeSink()
    sink.fail_load = True

    with pytest.raises(RuntimeError):
        run(source, store, sink, SMALL)

    assert store.get_json("state/article_read_history.json") is None  # 체크포인트는 그대로
    pending = store.get_json("state/article_read_history.pending.json")
    assert pending["rows"] == 40 and pending["max_id"] == 40

    sink.fail_load = False
    calls_before = len(source.calls)
    source.rows = []  # 재시도 때 MySQL을 읽어도 아무것도 안 나오게 해서, 파일만으로 복구됨을 확인

    result = run(source, store, sink, SMALL)

    assert result.recovered_run_id == pending["run_id"]
    assert sorted(sink.core) == list(range(1, 41))
    assert store.get_json("state/article_read_history.json")["last_id"] == 40
    assert store.get_json("state/article_read_history.pending.json") is None
    # 복구 뒤 이어지는 증분 조회 한 번(빈 결과)만 있었다
    assert len(source.calls) == calls_before + 1


def test_merge_failure_after_load_does_not_double_load_on_retry():
    source, store, sink = FakeSource(make_rows(1, 20)), FakeStore(), FakeSink()
    sink.fail_merge = True

    with pytest.raises(RuntimeError):
        run(source, store, sink, SMALL)
    assert len(sink.raw) == 20 and not sink.core
    failed_run_id = store.get_json("state/article_read_history.pending.json")["run_id"]

    sink.fail_merge = False
    run(source, store, sink, SMALL)

    # 실패한 실행 분량은 같은 job_id라 한 번만 적재된다(겹침 재조회 분량은 별도 실행)
    assert sum(1 for row in sink.raw if row["_run_id"] == failed_run_id) == 20
    assert sorted(sink.core) == list(range(1, 21))


def test_row_count_mismatch_blocks_checkpoint():
    class LossySink(FakeSink):
        def count_run(self, spec, run_id):
            return super().count_run(spec, run_id) - 1

    source, store, sink = FakeSource(make_rows(1, 5)), FakeStore(), LossySink()

    with pytest.raises(core.RowCountMismatch):
        run(source, store, sink, SMALL)

    assert store.get_json("state/article_read_history.json") is None
    assert not sink.core


# --- 전체 교체(snapshot) ---


def make_members(ids):
    return [{"id": i, "provider": "google", "created_at": datetime(2026, 11, 1, 0, 0, 0)} for i in ids]


def test_snapshot_replaces_core_and_reflects_updates_and_deletes():
    source, store, sink = FakeSource(make_members(range(1, 31))), FakeStore(), FakeSink()

    first = run(source, store, sink, SMALL, spec=core.MEMBER)
    assert first.rows == 30 and sorted(sink.core) == list(range(1, 31))

    rows = make_members([i for i in range(1, 34) if i != 7])  # 7번 탈퇴, 31~33 가입
    rows[0]["provider"] = "apple"  # 1번 값 변경
    source.rows = rows
    calls_before = len(source.calls)
    second = run(source, store, sink, SMALL, spec=core.MEMBER)

    assert second.rows == 32
    assert 7 not in sink.core and 33 in sink.core and len(sink.core) == 32
    assert sink.core[1]["provider"] == "apple"
    assert source.calls[calls_before] == (0, 10)  # 두 번째도 처음부터 읽는다
    assert len(sink.raw) == 30 + 32  # raw에는 날짜별 전체본이 쌓인다
    assert store.get_json("state/member.json")["rows"] == 32
    assert store.get_json("state/member.pending.json") is None


def test_snapshot_that_hits_a_limit_fails_without_touching_core():
    source, store, sink = FakeSource(make_members(range(1, 21))), FakeStore(), FakeSink()
    run(source, store, sink, SMALL, spec=core.MEMBER)

    source.rows = make_members(range(1, 101))
    capped = core.Settings(chunk_size=10, sleep_seconds=0, max_rows_per_run=30)
    with pytest.raises(core.SnapshotIncomplete):
        run(source, store, sink, capped, spec=core.MEMBER)

    assert sorted(sink.core) == list(range(1, 21))  # 일부만 읽은 것으로 바꾸지 않는다
    assert len(sink.raw) == 20


def test_snapshot_of_empty_source_keeps_core():
    source, store, sink = FakeSource(make_members(range(1, 6))), FakeStore(), FakeSink()
    run(source, store, sink, SMALL, spec=core.MEMBER)

    source.rows = []
    result = run(source, store, sink, SMALL, spec=core.MEMBER)

    assert result.rows == 0 and result.run_id is None
    assert sorted(sink.core) == [1, 2, 3, 4, 5]


def test_snapshot_failure_keeps_previous_core_and_next_run_recovers():
    source, store, sink = FakeSource(make_members(range(1, 6))), FakeStore(), FakeSink()
    run(source, store, sink, SMALL, spec=core.MEMBER)

    source.rows = make_members(range(1, 9))
    sink.fail_merge = True
    with pytest.raises(RuntimeError):
        run(source, store, sink, SMALL, spec=core.MEMBER)
    assert sorted(sink.core) == [1, 2, 3, 4, 5]

    sink.fail_merge = False
    run(source, store, sink, SMALL, spec=core.MEMBER)
    assert sorted(sink.core) == list(range(1, 9))


def test_table_chunk_size_overrides_settings():
    rows = [
        {"id": i, "member_id": 1, "newsletter_id": 2, "arrived_date_time": datetime(2026, 11, 1),
         "expected_read_time": 3, "created_at": datetime(2026, 11, 1)}
        for i in range(1, 1201)
    ]
    source, store, sink = FakeSource(rows), FakeStore(), FakeSink()
    naps = []

    core.run(core.ARTICLE, source, store, sink, core.Settings(), sleep=naps.append)

    assert all(limit == 500 for _, limit in source.calls)
    assert naps == [0.3, 0.3]
    assert len(sink.core) == 1200


def test_table_row_cap_applies_only_when_lower_than_settings():
    rows = [
        {"id": i, "member_id": 1, "newsletter_id": 2, "arrived_date_time": datetime(2026, 11, 1),
         "expected_read_time": 3, "created_at": datetime(2026, 11, 1)}
        for i in range(1, 25_001)
    ]
    quiet = dict(now=lambda: datetime(2026, 11, 19, 16, 0, 0, tzinfo=timezone.utc), sleep=lambda s: None)

    by_table = core.run(core.ARTICLE, FakeSource(rows), FakeStore(), FakeSink(), core.Settings(), **quiet)
    by_event = core.run(
        core.ARTICLE, FakeSource(rows), FakeStore(), FakeSink(), core.Settings(max_rows_per_run=1000), **quiet
    )

    assert by_table.rows == 20_000 and by_table.stop_reason == "max_rows_per_run"
    assert by_event.rows == 1000


def test_specs_leave_out_personal_and_body_columns():
    forbidden = {"email", "nickname", "birth_date", "gender", "age_group", "profile_image_url",
                 "provider_id", "contents", "contents_text", "title", "unsubscribe_url"}
    for spec in core.TABLES.values():
        assert not forbidden & set(spec.column_names), spec.name
        assert spec.key in spec.column_names
