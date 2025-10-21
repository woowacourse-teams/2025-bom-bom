-- Highlight 테이블에 member_id 및 newsletter_id 추가
ALTER TABLE highlight
    ADD COLUMN member_id       BIGINT NOT NULL,
    ADD COLUMN newsletter_id   BIGINT NULL,
    ADD COLUMN title           VARCHAR(255) NULL;

-- 기존 highlight.article_id -> article.id 기준으로 값 채우기
UPDATE highlight h
    JOIN article a ON a.id = h.article_id
    SET
        h.member_id     = a.member_id,
        h.newsletter_id = a.newsletter_id,
        h.title         = LEFT(a.title, 255);
