-- Subscribe에 unsubscribe_url 컬럼 추가
ALTER TABLE subscribe
    ADD COLUMN unsubscribe_url VARCHAR(512);
