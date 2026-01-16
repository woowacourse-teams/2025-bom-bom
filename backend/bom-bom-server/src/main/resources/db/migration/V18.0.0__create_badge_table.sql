CREATE TABLE badge
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    member_id      BIGINT       NOT NULL,
    badge_category VARCHAR(20)  NOT NULL,
    badge_type     VARCHAR(50)  NOT NULL,
    period_year    INT          NULL,
    period_month   INT          NULL,
    challenge_id   BIGINT       NULL,
    challenge_name VARCHAR(255) NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    INDEX idx_badge_member_id (member_id),
    INDEX idx_badge_category (badge_category),
    INDEX idx_badge_type (badge_type),
    INDEX idx_badge_created_at (created_at),
    INDEX idx_badge_member_category (member_id, badge_category),
    INDEX idx_badge_member_type (member_id, badge_type)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
