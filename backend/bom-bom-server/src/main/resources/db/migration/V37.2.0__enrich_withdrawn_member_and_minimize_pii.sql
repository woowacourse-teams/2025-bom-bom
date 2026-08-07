-- 탈퇴 회원 정보(WithdrawnMember) 보완
-- 1) 개인정보 최소화: 생년월일 제거
-- 2) 이탈 분석용 지표 추가: 가입 경로, 연령대, 마지막 활동일, 활동량 카운트

ALTER TABLE withdrawn_member
    DROP COLUMN birth_date,
    ADD COLUMN age_group VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    ADD COLUMN provider VARCHAR(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
    ADD COLUMN last_read_date DATE DEFAULT NULL,
    ADD COLUMN total_read_count INT NOT NULL DEFAULT 0,
    ADD COLUMN subscribe_count INT NOT NULL DEFAULT 0,
    ADD COLUMN challenge_count INT NOT NULL DEFAULT 0,
    ADD COLUMN badge_count INT NOT NULL DEFAULT 0,
    ADD COLUMN cleanup_completed BOOLEAN NOT NULL DEFAULT TRUE;
