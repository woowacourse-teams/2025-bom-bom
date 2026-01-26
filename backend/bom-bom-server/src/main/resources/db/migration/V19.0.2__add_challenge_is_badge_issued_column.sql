-- 챌린지 뱃지 발급 완료 여부를 관리하는 컬럼 추가
ALTER TABLE challenge 
ADD COLUMN is_badge_issued TINYINT(1) NOT NULL DEFAULT 0;
