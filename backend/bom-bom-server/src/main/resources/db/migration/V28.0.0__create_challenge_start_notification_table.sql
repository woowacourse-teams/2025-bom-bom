CREATE TABLE challenge_start_notification (
      id BIGINT NOT NULL AUTO_INCREMENT,
      member_id BIGINT NOT NULL,
      status VARCHAR(32) NOT NULL,
      attempts INT NOT NULL DEFAULT 0,
      next_retry_at DATETIME(6) NULL,
      last_error VARCHAR(1024) NULL,
      challenge_id BIGINT NOT NULL,
      challenge_name VARCHAR(255) NOT NULL,
      created_at DATETIME(6) NOT NULL,
      updated_at DATETIME(6) NOT NULL,
      PRIMARY KEY (id),
      CONSTRAINT uk_challenge_start_notification_challenge_member UNIQUE (challenge_id, member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
