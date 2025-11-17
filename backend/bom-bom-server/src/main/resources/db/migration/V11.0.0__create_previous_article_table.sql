CREATE TABLE `previous_article` (
                           `expected_read_time` tinyint DEFAULT NULL,
                           `arrived_date_time` datetime(6) NOT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `newsletter_id` bigint NOT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `contents_summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                           `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                           `contents` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                           PRIMARY KEY (`id`)
);
