-- monthly_reading 테이블의 rank 컬럼을 rankOrder로 수정
ALTER TABLE monthly_reading
  CHANGE COLUMN `rank` rank_order INT NOT NULL;
