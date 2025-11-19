CREATE TABLE previous_article (
                        id bigint NOT NULL AUTO_INCREMENT PRIMARY KEY,
                        expected_read_time tinyint DEFAULT NULL,
                        arrived_date_time datetime(6) NOT NULL,
                        newsletter_id bigint NOT NULL,
                        contents_summary varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                        title varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                        contents mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                        is_fixed boolean NOT NULL DEFAULT false,
                        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE newsletter_previous_policy (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        newsletter_id BIGINT NOT NULL,
                        strategy ENUM('FIXED_WITH_LATEST', 'FIXED_ONLY', 'LATEST_ONLY', 'INACTIVE') NOT NULL DEFAULT 'INACTIVE',
                        latest_count TINYINT NOT NULL DEFAULT 5,
                        fixed_count TINYINT NOT NULL DEFAULT 0,
                        exposure_ratio TINYINT NOT NULL DEFAULT 100,
                        created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
                        updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
                        CONSTRAINT uk_newsletter_previous_article_policy_newsletter_id UNIQUE (newsletter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
