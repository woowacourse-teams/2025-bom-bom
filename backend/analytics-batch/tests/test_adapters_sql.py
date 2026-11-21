from analytics_batch import core
from analytics_batch.adapters import BigQuerySink


def sink():
    return BigQuerySink(project="p", location="l", raw_dataset="raw", core_dataset="core", session=object())


def test_replace_sql_swaps_core_with_one_run():
    sql = sink().replace_sql(core.SUBSCRIBE)

    assert sql.startswith("CREATE OR REPLACE TABLE `p.core.subscribe`\nCLUSTER BY member_id\nAS\n")
    assert "FROM `p.raw.subscribe`\nWHERE _run_id = @run_id" in sql
    assert "PARTITION BY" not in sql
    assert "SELECT id, member_id, newsletter_id, status, created_at, updated_at, _run_id, _extracted_at" in sql


def test_core_ddl_keeps_partition_for_incremental_tables():
    ddl = sink().core_ddl(core.ARTICLE)

    assert ddl.endswith(")\nPARTITION BY DATE(arrived_date_time)\nCLUSTER BY member_id")
    assert "contents" not in ddl


def test_core_ddl_for_existing_table_is_unchanged():
    ddl = sink().core_ddl(core.ARTICLE_READ_HISTORY)

    assert ddl.endswith("_extracted_at TIMESTAMP\n)\nPARTITION BY DATE(read_at)\nCLUSTER BY member_id")
