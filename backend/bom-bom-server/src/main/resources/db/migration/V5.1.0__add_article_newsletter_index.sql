-- article 테이블에서 newsletter_id로 조인을 항상 해서 추가 필요
CREATE INDEX idx_article_member_newsletter ON article (member_id, newsletter_id);

-- newsletter 테이블에 category_id 인덱스 추가
CREATE INDEX idx_newsletter_category_id ON newsletter (category_id, id);
