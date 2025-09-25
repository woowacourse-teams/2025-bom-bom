CREATE TABLE monthly_reading_realtime (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,
    current_count SMALLINT NOT NULL DEFAULT 0
);
