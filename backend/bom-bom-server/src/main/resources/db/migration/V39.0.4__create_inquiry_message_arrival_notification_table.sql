CREATE TABLE inquiry_message_arrival_notification (
      id BIGINT NOT NULL AUTO_INCREMENT,
      member_id BIGINT NOT NULL,
      status VARCHAR(32) NOT NULL,
      attempts INT NOT NULL DEFAULT 0,
      next_retry_at DATETIME(6) NULL,
      last_error VARCHAR(1024) NULL,
      room_id BIGINT NOT NULL,
      content VARCHAR(20) NOT NULL,
      created_at DATETIME(6) NOT NULL,
      updated_at DATETIME(6) NOT NULL,
      PRIMARY KEY (id),
      INDEX idx_inquiry_message_arrival_notification_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
