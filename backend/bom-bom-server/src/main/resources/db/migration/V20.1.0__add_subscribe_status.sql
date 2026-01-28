-- status 컬럼 추가 (기본값: SUBSCRIBED)
ALTER TABLE subscribe ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'SUBSCRIBED';

-- 기존 데이터 명시적 업데이트
UPDATE subscribe SET status = 'SUBSCRIBED' WHERE status IS NULL OR status = '';
