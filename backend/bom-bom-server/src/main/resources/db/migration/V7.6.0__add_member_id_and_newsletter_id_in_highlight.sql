-- Highlight 테이블에 member_id 및 newsletter_id 추가
ALTER TABLE highlight
    ADD COLUMN member_id       BIGINT NOT NULL,
    ADD COLUMN newsletter_id   BIGINT NOT NULL,
    ADD COLUMN title           VARCHAR(255) NOT NULL;

-- 기존 highlight.article_id -> article.id 기준으로 값 채우기
UPDATE highlight h
    LEFT JOIN article a ON a.id = h.article_id
    SET
        h.member_id     = COALESCE(a.member_id, 0),
        h.newsletter_id = COALESCE(a.newsletter_id, 0),
        h.title         = COALESCE(LEFT(a.title, 255), 'Untitled');
