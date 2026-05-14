START TRANSACTION;
-- 탈퇴 되어있던 데이터들 모두 제거
DELETE FROM member WHERE deleted_at IS NOT NULL;
-- member 테이블에서 deleted_at 필드 제거
ALTER TABLE member DROP COLUMN deleted_at;
COMMIT;
