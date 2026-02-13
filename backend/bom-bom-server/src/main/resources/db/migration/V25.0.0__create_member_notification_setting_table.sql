CREATE TABLE `member_notification_setting` (
   `member_id`       bigint       NOT NULL,
   `article_enabled` tinyint(1)   NOT NULL DEFAULT '1',
   `event_enabled`   tinyint(1)   NOT NULL DEFAULT '0',
   `created_at`      datetime(6)  DEFAULT NULL,
   `updated_at`      datetime(6)  DEFAULT NULL,
   PRIMARY KEY (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
