CREATE TABLE unsubscribe_retry (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscribe_id BIGINT NOT NULL,
    retry_count INTEGER NOT NULL DEFAULT 0,
    next_retry_at DATETIME(6) NOT NULL,
    last_error VARCHAR(255),
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    CONSTRAINT uk_unsubscribe_retry_subscribe_id UNIQUE (subscribe_id)
) ENGINE=InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

CREATE TABLE unsubscribe_pattern (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    pattern_key VARCHAR(50) NOT NULL,
    pattern_value TEXT      NOT NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_unsubscribe_pattern_key UNIQUE (pattern_key)
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci
