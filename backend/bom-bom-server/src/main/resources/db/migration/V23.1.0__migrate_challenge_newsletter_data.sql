-- 기존 challenge_newsletter 데이터를 newsletter_group_item으로 마이그레이션
-- 모든 챌린지가 동일한 뉴스레터 조합을 사용하므로 단일 그룹 생성

-- 단일 그룹 생성 ('매일 발송')
INSERT INTO newsletter_group (name, created_at, updated_at) 
VALUES ('매일 발송', NOW(), NOW());

-- 생성된 그룹의 ID를 변수에 저장
SET @default_group_id = LAST_INSERT_ID();

-- 모든 challenge에 newsletter_group_id 할당
UPDATE challenge 
SET newsletter_group_id = @default_group_id;

-- newsletter_group_item 마이그레이션
-- 기존 challenge_newsletter에서 고유한 newsletter_id만 가져와서 새 그룹에 연결
INSERT INTO newsletter_group_item (newsletter_group_id, newsletter_id)
SELECT DISTINCT @default_group_id, newsletter_id
FROM challenge_newsletter;
