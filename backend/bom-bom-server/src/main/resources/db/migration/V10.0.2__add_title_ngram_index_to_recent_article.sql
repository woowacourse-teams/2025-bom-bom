-- recent_article 테이블의 title 컬럼에 ngram FULLTEXT 인덱스 추가
-- title과 contents_text 모두 ngram FULLTEXT 인덱스를 사용하여 검색 성능 최적화
CREATE FULLTEXT INDEX `idx_recent_article_title_ngram` 
ON `recent_article`(`title`) 
WITH PARSER ngram;

