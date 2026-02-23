-- newsletter 테이블에 status 컬럼 추가 (ACTIVE: 발행중, SUSPENDED: 휴재, DISCONTINUED: 폐간)
ALTER TABLE newsletter
    ADD COLUMN status ENUM('ACTIVE', 'SUSPENDED', 'DISCONTINUED') NOT NULL DEFAULT 'ACTIVE';
