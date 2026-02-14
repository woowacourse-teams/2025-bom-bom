CREATE TABLE `member_notification_setting` (
    `id`             BIGINT       NOT NULL AUTO_INCREMENT,
    `member_id`      BIGINT       NOT NULL,
    `category`       VARCHAR(255) NOT NULL,
    `is_enabled`     TINYINT(1)   NOT NULL,
    `created_at`     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_member_category` (`member_id`, `category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
