-- BomBom 초기 데이터베이스 스키마 생성

-- Role 테이블 (Member가 참조하므로 먼저 생성)
CREATE TABLE `role` (
                        `id` bigint NOT NULL AUTO_INCREMENT,
                        `authority` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                        PRIMARY KEY (`id`)
);

-- Member 테이블
CREATE TABLE `member` (
                          `birth_date` date DEFAULT NULL,
                          `created_at` datetime(6) DEFAULT NULL,
                          `id` bigint NOT NULL AUTO_INCREMENT,
                          `role_id` bigint NOT NULL DEFAULT '0',
                          `updated_at` datetime(6) DEFAULT NULL,
                          `email` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `profile_image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                          `nickname` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `provider` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `provider_id` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `gender` enum('FEMALE','MALE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                          `deleted_at` datetime(6) DEFAULT NULL,
                          PRIMARY KEY (`id`),
                          UNIQUE KEY `UKgyl5t9mxo4q408lurfmndln0b` (`provider`,`provider_id`),
                          UNIQUE KEY `UKhh9kg6jti4n1eoiertn2k6qsc` (`nickname`)
);

CREATE TABLE `article` (
                           `expected_read_time` tinyint DEFAULT NULL,
                           `is_read` tinyint(1) DEFAULT '0',
                           `arrived_date_time` datetime(6) NOT NULL,
                           `created_at` datetime(6) DEFAULT NULL,
                           `id` bigint NOT NULL AUTO_INCREMENT,
                           `member_id` bigint NOT NULL,
                           `newsletter_id` bigint NOT NULL,
                           `updated_at` datetime(6) DEFAULT NULL,
                           `thumbnail_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                           `contents_summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                           `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                           `contents` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                           PRIMARY KEY (`id`)
);

-- Category 테이블 (Newsletter가 참조하므로 먼저 생성)
CREATE TABLE `category` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                            PRIMARY KEY (`id`)
);
-- NewsletterDetail 테이블 (Newsletter가 참조하므로 먼저 생성)
CREATE TABLE `newsletter_detail` (
                                     `id` bigint NOT NULL AUTO_INCREMENT,
                                     `main_page_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                     `subscribe_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                     `issue_cycle` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                     `subscribe_count` bigint NOT NULL DEFAULT '0',
                                     `sender` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'unknown',
                                     `subscribePageImageUrl` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                     `previousNewsletterUrl` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                     `previous_newsletter_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                     `subscribe_page_image_url` varchar(512) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                                     PRIMARY KEY (`id`)
);

-- Newsletter 테이블
CREATE TABLE `newsletter` (
                              `category_id` bigint NOT NULL,
                              `created_at` datetime(6) DEFAULT NULL,
                              `detail_id` bigint NOT NULL,
                              `id` bigint NOT NULL AUTO_INCREMENT,
                              `updated_at` datetime(6) DEFAULT NULL,
                              `email` varchar(60) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `image_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `description` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              `name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                              PRIMARY KEY (`id`)
);



-- Subscribe 테이블
CREATE TABLE `subscribe` (
                             `created_at` datetime(6) DEFAULT NULL,
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `member_id` bigint NOT NULL,
                             `newsletter_id` bigint NOT NULL,
                             `updated_at` datetime(6) DEFAULT NULL,
                             PRIMARY KEY (`id`)
);

-- Bookmark 테이블
CREATE TABLE `bookmark` (
                            `id` bigint NOT NULL AUTO_INCREMENT,
                            `created_at` datetime(6) DEFAULT NULL,
                            `updated_at` datetime(6) DEFAULT NULL,
                            `article_id` bigint NOT NULL,
                            `member_id` bigint NOT NULL,
                            PRIMARY KEY (`id`),
                            UNIQUE KEY `UKn4tjskat1leb7wje1gaeg3gio` (`member_id`,`article_id`)
);

-- Highlight 테이블
CREATE TABLE `highlight` (
                             `id` bigint NOT NULL AUTO_INCREMENT,
                             `created_at` datetime(6) DEFAULT NULL,
                             `updated_at` datetime(6) DEFAULT NULL,
                             `article_id` bigint NOT NULL,
                             `color` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `end_offset` int DEFAULT NULL,
                             `endxpath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             `start_offset` int DEFAULT NULL,
                             `startxpath` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             `text` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                             `memo` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
                             PRIMARY KEY (`id`)
);

-- Stage 테이블 (Pet가 참조하므로 먼저 생성)
CREATE TABLE `stage` (
                         `id` bigint NOT NULL AUTO_INCREMENT,
                         `created_at` datetime(6) DEFAULT NULL,
                         `updated_at` datetime(6) DEFAULT NULL,
                         `level` int NOT NULL,
                         `required_score` int NOT NULL,
                         PRIMARY KEY (`id`)
);

-- Pet 테이블
CREATE TABLE `pet` (
                       `id` bigint NOT NULL AUTO_INCREMENT,
                       `created_at` datetime(6) DEFAULT NULL,
                       `updated_at` datetime(6) DEFAULT NULL,
                       `current_score` int NOT NULL DEFAULT '0',
                       `member_id` bigint NOT NULL,
                       `stage_id` bigint NOT NULL,
                       `is_attended` tinyint(1) NOT NULL DEFAULT '0',
                       PRIMARY KEY (`id`)
);

-- Reading 관련 테이블들
CREATE TABLE `today_reading` (
                                 `current_count` tinyint NOT NULL DEFAULT '0',
                                 `total_count` tinyint NOT NULL,
                                 `created_at` datetime(6) DEFAULT NULL,
                                 `id` bigint NOT NULL AUTO_INCREMENT,
                                 `member_id` bigint NOT NULL,
                                 `updated_at` datetime(6) DEFAULT NULL,
                                 PRIMARY KEY (`id`),
                                 UNIQUE KEY `UKcvjl8m00v958hvtkjmr14owr2` (`member_id`)
);

CREATE TABLE `weekly_reading` (
                                  `current_count` tinyint NOT NULL DEFAULT '0',
                                  `goal_count` tinyint NOT NULL,
                                  `created_at` datetime(6) DEFAULT NULL,
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `member_id` bigint NOT NULL,
                                  `updated_at` datetime(6) DEFAULT NULL,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `UKhrfngbshlk3ve70h08aw61k9r` (`member_id`)
);


CREATE TABLE `monthly_reading` (
                                   `id` bigint NOT NULL AUTO_INCREMENT,
                                   `current_count` smallint NOT NULL,
                                   `member_id` bigint NOT NULL,
                                   PRIMARY KEY (`id`),
                                   UNIQUE KEY `UKivdb27aujnw4u223qhbhwonmn` (`member_id`)
);

CREATE TABLE `yearly_reading` (
                                  `id` bigint NOT NULL AUTO_INCREMENT,
                                  `current_count` smallint NOT NULL,
                                  `member_id` bigint NOT NULL,
                                  `reading_year` int NOT NULL,
                                  PRIMARY KEY (`id`),
                                  UNIQUE KEY `unique_member_id_year` (`member_id`,`reading_year`)
);

CREATE TABLE `continue_reading` (
                                    `day_count` smallint NOT NULL,
                                    `created_at` datetime(6) DEFAULT NULL,
                                    `id` bigint NOT NULL AUTO_INCREMENT,
                                    `member_id` bigint NOT NULL,
                                    `updated_at` datetime(6) DEFAULT NULL,
                                    PRIMARY KEY (`id`),
                                    UNIQUE KEY `UK1vroxowpjl9xad0kus652hwfx` (`member_id`)
);
