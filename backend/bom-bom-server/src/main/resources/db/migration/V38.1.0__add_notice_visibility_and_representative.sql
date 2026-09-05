-- Notice에 공개 여부(visibility)와 대표 공지 여부(is_representative) 컬럼 추가
-- 기존 공지는 모두 공개(PUBLIC), 대표 아님(false)으로 백필
ALTER TABLE notice
    ADD COLUMN visibility ENUM('PRIVATE', 'PUBLIC') NOT NULL DEFAULT 'PUBLIC',
    ADD COLUMN is_representative BOOLEAN NOT NULL DEFAULT FALSE;

-- 기존 데이터 중 가장 최근에 등록된 공지 1건을 대표 공지로 설정
-- (대표 공지는 전체에서 1건만 허용)
UPDATE notice
SET is_representative = TRUE
ORDER BY created_at DESC, id DESC
LIMIT 1;
