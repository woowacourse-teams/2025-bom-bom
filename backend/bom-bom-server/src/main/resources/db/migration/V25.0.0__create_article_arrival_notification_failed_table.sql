CREATE TABLE `article_arrival_notification_failed` (
       `id`                         BIGINT          NOT NULL AUTO_INCREMENT,
       `original_notification_id`   BIGINT          NOT NULL,
       `member_id`                  BIGINT          NOT NULL,
       `article_id`                 BIGINT          NOT NULL,
       `newsletter_name`            VARCHAR(255)    NOT NULL,
       `article_title`              VARCHAR(255)    NOT NULL,
       `final_attempts`             INT             NOT NULL,
       `last_error`                 VARCHAR(1024),
       `failed_at`                  DATETIME(6)     NOT NULL,
       `created_at`                 DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
       `updated_at`                 DATETIME(6)     NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
       PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
