CREATE TABLE continue_reading_shield
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    member_id       BIGINT      NOT NULL,
    remaining_count SMALLINT    NOT NULL DEFAULT 0,
    created_at      DATETIME(6) DEFAULT NULL,
    updated_at      DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_continue_reading_shield_member_id UNIQUE (member_id)
);

CREATE TABLE continue_reading_shield_history
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    type       VARCHAR(20) NOT NULL,
    event_date DATE        NOT NULL,
    quantity   SMALLINT    NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_continue_reading_shield_history_member_type_date
        UNIQUE (member_id, type, event_date)
);

INSERT INTO continue_reading_shield (
    member_id,
    remaining_count,
    created_at,
    updated_at
)
SELECT
    id,
    1,
    NOW(6),
    NOW(6)
FROM member;

INSERT INTO continue_reading_shield_history (
    member_id,
    type,
    event_date,
    quantity,
    created_at,
    updated_at
)
SELECT
    id,
    'GRANT',
    DATE_FORMAT(UTC_TIMESTAMP(6) + INTERVAL 9 HOUR, '%Y-%m-01'),
    1,
    NOW(6),
    NOW(6)
FROM member;
