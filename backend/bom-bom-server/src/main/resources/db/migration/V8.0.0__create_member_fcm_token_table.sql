CREATE TABLE member_fcm_token (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  member_id BIGINT NOT NULL,
  device_uuid VARCHAR(255) NOT NULL,
  fcm_token VARCHAR(300) NOT NULL,
  is_notification_enabled BOOLEAN DEFAULT TRUE,
  created_at DATETIME NOT NULL,
  updated_at DATETIME NOT NULL,

    -- 유니크 제약조건: 한 멤버는 같은 디바이스에 대해 하나의 토큰만 가질 수 있음
      UNIQUE KEY uk_member_device (member_id, device_uuid),

    -- 인덱스: 멤버별 토큰 조회 최적화
      INDEX idx_member_id (member_id),

    -- 인덱스: 디바이스별 토큰 조회 최적화
      INDEX idx_device_uuid (device_uuid)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
