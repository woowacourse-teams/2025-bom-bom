-- 1. newsletter_group 테이블 생성
CREATE TABLE newsletter_group
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 2. newsletter_group_item 테이블 생성
CREATE TABLE newsletter_group_item
(
    id                 BIGINT NOT NULL AUTO_INCREMENT,
    newsletter_group_id BIGINT NOT NULL,
    newsletter_id       BIGINT NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_newsletter_group_item (newsletter_group_id, newsletter_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- 3. challenge 테이블에 newsletter_group_id 컬럼 추가 (nullable로 먼저 추가)
ALTER TABLE challenge
    ADD COLUMN newsletter_group_id BIGINT NULL;

-- 4. 기존 challenge_newsletter 데이터를 newsletter_group_item으로 마이그레이션
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

-- 5. challenge.newsletter_group_id를 NOT NULL로 변경
ALTER TABLE challenge
    MODIFY COLUMN newsletter_group_id BIGINT NOT NULL;

-- 6. challenge_newsletter 테이블 삭제
DROP TABLE challenge_newsletter;
