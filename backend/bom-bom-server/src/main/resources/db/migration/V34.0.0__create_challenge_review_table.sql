CREATE TABLE challenge_review
(
    id           BIGINT         NOT NULL AUTO_INCREMENT,
    challenge_id BIGINT         NOT NULL,
    member_id    BIGINT         NOT NULL,
    comment      VARCHAR(500)   NOT NULL,
    is_private   TINYINT(1)     NOT NULL DEFAULT 0,
    created_at   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)    NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_challenge_review_challenge_id_member_id (challenge_id, member_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
