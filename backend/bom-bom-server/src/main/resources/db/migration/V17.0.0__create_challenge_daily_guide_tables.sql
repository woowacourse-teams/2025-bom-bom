CREATE TABLE challenge_daily_guide
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    challenge_id    BIGINT       NOT NULL,
    day_index       INT          NOT NULL,
    type            VARCHAR(20)  NOT NULL,
    image_url       VARCHAR(2048) NOT NULL,
    notice          VARCHAR(1000) NULL,
    comment_enabled TINYINT(1)   NOT NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenge_daily_guide (challenge_id, day_index)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_daily_guide_comment
(
    id              BIGINT       NOT NULL AUTO_INCREMENT,
    guide_id        BIGINT       NOT NULL,
    participant_id  BIGINT       NOT NULL,
    content         VARCHAR(1000) NOT NULL,
    created_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at      DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenge_daily_guide_comment (guide_id, participant_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

