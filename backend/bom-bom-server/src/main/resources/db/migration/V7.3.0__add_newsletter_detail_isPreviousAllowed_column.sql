-- NewsletterDetail에 is_previous_allowed 컬럼 추가. 기본값은 false로 비허용

ALTER TABLE newsletter_detail
    ADD COLUMN is_previous_allowed BOOLEAN NOT NULL DEFAULT FALSE;
