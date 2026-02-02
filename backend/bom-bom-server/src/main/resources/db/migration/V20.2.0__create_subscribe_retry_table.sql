CREATE TABLE subscribe_retry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscribe_id BIGINT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at DATETIME(6) NOT NULL,
    last_error VARCHAR(255),
    created_at DATETIME(6) NOT NULL,
    modified_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_subscribe_retry_subscribe_id UNIQUE (subscribe_id)
);

CREATE INDEX idx_subscribe_retry_next_retry_at ON subscribe_retry (next_retry_at);
