CREATE TABLE coupon_issue (
      id BIGINT AUTO_INCREMENT PRIMARY KEY,
      member_id BIGINT NOT NULL,
      coupon_name VARCHAR(100) NOT NULL,
      image_url VARCHAR(2048) NOT NULL,
      created_at DATETIME NOT NULL,
      updated_at DATETIME NOT NULL,

      UNIQUE KEY uk_member_coupon (member_id, coupon_name),
      INDEX idx_member_id (member_id),
      INDEX idx_coupon_name (coupon_name)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
