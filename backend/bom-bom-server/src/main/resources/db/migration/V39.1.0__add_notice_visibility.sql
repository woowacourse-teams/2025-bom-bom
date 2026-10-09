-- Notice에 공개 여부(visibility) 컬럼 추가
-- 기존 공지는 모두 공개(PUBLIC)로 백필
ALTER TABLE notice
    ADD COLUMN visibility ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PUBLIC';
