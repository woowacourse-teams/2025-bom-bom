-- RecentArticle 테이블에 article_id 컬럼 추가
ALTER TABLE `recent_article` 
ADD COLUMN `article_id` BIGINT NOT NULL AFTER `id`;
