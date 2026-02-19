CREATE TABLE challenge_todo_reminder_notification (
        id BIGINT NOT NULL AUTO_INCREMENT,
        member_id BIGINT NOT NULL,
        status VARCHAR(255) NOT NULL,
        attempts INT NOT NULL DEFAULT 0,
        next_retry_at DATETIME(6) NULL,
        last_error VARCHAR(1024) NULL,
        created_at DATETIME(6) NOT NULL,
        updated_at DATETIME(6) NOT NULL,
        challenge_id BIGINT NOT NULL,
        challenge_name VARCHAR(255) NOT NULL,
        PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
