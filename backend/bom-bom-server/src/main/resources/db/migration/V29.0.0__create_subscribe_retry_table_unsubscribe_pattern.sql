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
) ENGINE = InnoDB DEFAULT CHARSET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci;

INSERT INTO unsubscribe_pattern (pattern_key, pattern_value) VALUES
('unsubscribe-pattern', 'unsubscribe|구독.?취소|수신.?거부|해지|cancel|confirm|yes'),
('success-pattern', 'success|unsubscribed|canceled|cancelled|no.?longer|removed|successfully|취소.?완료|처리.?완료|해지.?완료|거부.?완료|취소.?되었습니다|해지.?되었습니다|성공|완료'),
('already-unsubscribed-pattern', 'already.?unsubscribed|not.?subscribed|no.?longer|구독.?중인.?이메일.?주소가.?아닙니다|이미.?구독.?취소|이미.?취소|이미.?수신.?거부|구독.?취소.?되었습니다|해지.?되었습니다'),
('error-pattern', 'error|오류|실패|failed|invalid|잘못|문제'),
('ad-domains', 'google, doubleclick, adservice');
