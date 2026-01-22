-- 챌린지 코멘트 좋아요 테이블 생성
CREATE TABLE IF NOT EXISTS challenge_comment_like (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    participant_id BIGINT       NOT NULL,
    comment_id     BIGINT       NOT NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),

    PRIMARY KEY (id),

    CONSTRAINT uk_challenge_comment_like_participant_comment
    UNIQUE (participant_id, comment_id),

    KEY idx_challenge_comment_like_comment_id (comment_id)
    ) ENGINE=InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_0900_ai_ci;
