CREATE TABLE continue_reading_shield
(
    id              BIGINT      NOT NULL AUTO_INCREMENT,
    member_id       BIGINT      NOT NULL,
    remaining_count TINYINT     NOT NULL DEFAULT 0,
    created_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_continue_reading_shield_member_id UNIQUE (member_id)
);

CREATE TABLE continue_reading_shield_history
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    type       VARCHAR(20) NOT NULL,
    reason     VARCHAR(30) NOT NULL,
    event_date DATE        NOT NULL,
    quantity   TINYINT     NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_continue_reading_shield_history_member_type_reason_date
        UNIQUE (member_id, type, reason, event_date)
);

INSERT INTO continue_reading_shield (
    member_id,
    remaining_count
)
SELECT
    id,
    1
FROM member;

INSERT INTO continue_reading_shield_history (
    member_id,
    type,
    reason,
    event_date,
    quantity
)
SELECT
    id,
    'GRANT',
    'MIGRATION_BACKFILL',
    DATE_FORMAT(UTC_TIMESTAMP(6) + INTERVAL 9 HOUR, '%Y-%m-01'),
    1
FROM member;
