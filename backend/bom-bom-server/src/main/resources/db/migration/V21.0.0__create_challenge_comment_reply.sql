CREATE TABLE challenge_comment_reply (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    participant_id BIGINT       NOT NULL,
    comment_id     BIGINT       NOT NULL,
    reply          VARCHAR(500) NOT NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_0900_ai_ci;
