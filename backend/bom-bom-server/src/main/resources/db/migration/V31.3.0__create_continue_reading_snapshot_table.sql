CREATE TABLE `continue_reading_snapshot` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `member_id` bigint NOT NULL,
    `day_count` smallint NOT NULL,
    `rank_order` bigint NOT NULL,
    `created_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` datetime(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_continue_reading_snapshot_member_id` (`member_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
