-- NewsletterDetail에 subscribe_method 컬럼 추가

ALTER TABLE newsletter_detail
    ADD COLUMN subscribe_method VARCHAR(512) NULL;
