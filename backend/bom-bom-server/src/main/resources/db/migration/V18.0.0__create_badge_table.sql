CREATE TABLE badge
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    member_id      BIGINT       NOT NULL,
    badge_category VARCHAR(20)  NOT NULL,
    badge_grade    VARCHAR(20)  NULL,
    period_year    INT          NULL,
    period_month   INT          NULL,
    challenge_id          BIGINT       NULL,
    challenge_name        VARCHAR(255) NULL,
    challenge_generation  INT          NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
