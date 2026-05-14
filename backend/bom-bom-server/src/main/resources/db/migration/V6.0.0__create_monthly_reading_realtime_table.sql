CREATE TABLE monthly_reading_realtime (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,
    current_count SMALLINT NOT NULL DEFAULT 0
);

ALTER TABLE monthly_reading RENAME TO monthly_reading_snapshot;
