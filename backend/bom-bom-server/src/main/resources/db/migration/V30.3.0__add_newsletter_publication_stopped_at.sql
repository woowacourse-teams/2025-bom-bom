-- newsletter 테이블에 발행 중단일 컬럼 추가
ALTER TABLE newsletter
    ADD COLUMN suspended_at DATE NULL;
