ALTER TABLE continue_reading_realtime
    ADD COLUMN max_day_count SMALLINT NOT NULL DEFAULT 0;

UPDATE continue_reading_realtime
SET max_day_count = day_count;
