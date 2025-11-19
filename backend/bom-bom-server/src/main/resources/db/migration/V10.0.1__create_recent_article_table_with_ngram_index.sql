-- RecentArticle 테이블 생성 (최근 5일 데이터용, ngram 인덱스 사용)
CREATE TABLE `recent_article` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `title` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `contents` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `contents_text` mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `thumbnail_url` varchar(512) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    `expected_read_time` tinyint DEFAULT NULL,
    `contents_summary` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    `is_read` tinyint(1) DEFAULT '0',
    `member_id` bigint NOT NULL,
    `newsletter_id` bigint NOT NULL,
    `arrived_date_time` datetime(6) NOT NULL,
    `created_at` datetime(6) DEFAULT NULL,
    `updated_at` datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`),
    
    -- 일반 인덱스: 멤버별 + 날짜별 조회 최적화
    INDEX `idx_recent_article_member_arrived` (`member_id`, `arrived_date_time`),
    
    -- 일반 인덱스: 뉴스레터별 조회 최적화
    INDEX `idx_recent_article_newsletter` (`newsletter_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ngram FULLTEXT 인덱스 생성 (검색 성능 최적화)
-- ngram 파서를 사용하여 한글 검색 성능 향상
CREATE FULLTEXT INDEX `idx_recent_article_contents_text_ngram` 
ON `recent_article`(`contents_text`) 
WITH PARSER ngram;

