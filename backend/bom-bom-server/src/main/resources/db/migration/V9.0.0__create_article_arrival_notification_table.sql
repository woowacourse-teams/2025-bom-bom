CREATE TABLE article_arrival_notification (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      member_id BIGINT NOT NULL,
      article_id BIGINT NOT NULL,
      newsletter_name VARCHAR(255) NOT NULL,
      article_title VARCHAR(500) NOT NULL,
      status ENUM('PENDING', 'SENT', 'FAILED') NOT NULL DEFAULT 'PENDING',
      attempts INT DEFAULT 0,
      next_retry_at DATETIME NULL,
      last_error VARCHAR(1024) NULL,
      is_read BOOLEAN DEFAULT FALSE,
      created_at DATETIME NOT NULL,
      updated_at DATETIME NOT NULL,

    -- 인덱스: 멤버별 알림 조회 최적화
      INDEX idx_member_id (member_id),

    -- 인덱스: 상태별 알림 조회 최적화 (스케줄러에서 사용)
      INDEX idx_status (status)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
