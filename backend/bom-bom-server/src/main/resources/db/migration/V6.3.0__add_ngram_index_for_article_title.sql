-- Article 테이블의 title 컬럼에 ngram 인덱스 추가
-- MySQL ngram 파서를 사용하여 한글 검색 성능 향상

-- ngram 파서를 사용한 FULLTEXT 인덱스 생성
ALTER TABLE article ADD FULLTEXT INDEX idx_article_title_ngram (title) WITH PARSER ngram;
