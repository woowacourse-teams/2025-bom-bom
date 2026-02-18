CREATE TABLE coupon_issue
(
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    member_id   BIGINT        NULL,
    coupon_name VARCHAR(100)  NOT NULL,
    image_url   VARCHAR(2048) NOT NULL,
    created_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6)   NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_coupon (member_id, coupon_name)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
