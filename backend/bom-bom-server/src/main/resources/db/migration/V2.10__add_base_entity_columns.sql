-- BaseEntity 컬럼 추가 (created_at, updated_at)
-- MonthlyReading 테이블에 BaseEntity 컬럼 추가
ALTER TABLE monthly_reading 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- YearlyReading 테이블에 BaseEntity 컬럼 추가
ALTER TABLE yearly_reading 
ADD COLUMN created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
ADD COLUMN updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

-- 기존 데이터의 created_at을 현재 시간으로 설정 (updated_at은 자동으로 현재 시간이 됨)
UPDATE monthly_reading SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
UPDATE yearly_reading SET created_at = CURRENT_TIMESTAMP WHERE created_at IS NULL;
