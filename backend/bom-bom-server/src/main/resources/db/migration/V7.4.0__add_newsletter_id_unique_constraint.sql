-- Newsletter에 detail id 유니크 제약 추가

ALTER TABLE newsletter
    ADD CONSTRAINT uk_newsletter_detail_id UNIQUE (detail_id);
