from analytics_batch import handler


def test_event_can_lower_the_row_cap_for_a_manual_run(monkeypatch):
    monkeypatch.setenv("MAX_ROWS_PER_RUN", "200000")

    assert handler.settings_from_env({"max_rows_per_run": 20000}).max_rows_per_run == 20000
    assert handler.settings_from_env({}).max_rows_per_run == 200000
    assert handler.settings_from_env(None).max_rows_per_run == 200000
