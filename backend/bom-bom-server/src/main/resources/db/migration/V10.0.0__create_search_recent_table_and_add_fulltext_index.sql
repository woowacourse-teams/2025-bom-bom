-- search_recent 테이블 생성 (최근 3일 검색용, n-gram 인덱스)
CREATE TABLE `search_recent` (
    `id` BIGINT NOT NULL AUTO_INCREMENT,
    `article_id` BIGINT NOT NULL,
    `member_id` BIGINT NOT NULL,
    `newsletter_id` BIGINT NOT NULL,
    `title` VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `contents` MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `arrived_date_time` DATETIME(6) NOT NULL,
    `created_at` DATETIME(6) DEFAULT NULL,
    `updated_at` DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_article_id` (`article_id`),
    INDEX `idx_member_id` (`member_id`),
    INDEX `idx_arrived_date_time` (`arrived_date_time`),
    -- n-gram 인덱스 (최근 검색용)
    FULLTEXT INDEX `ft_title_contents_ngram` (`title`, `contents`) WITH PARSER ngram
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- article 테이블에 Full-Text 인덱스 추가 (과거 검색용)
ALTER TABLE `article` 
ADD FULLTEXT INDEX `ft_title_contents` (`title`, `contents`);
