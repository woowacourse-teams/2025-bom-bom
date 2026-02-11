-- newsletter_group 테이블 생성
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

-- newsletter_group_item 테이블 생성
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

-- challenge 테이블에 newsletter_group_id 컬럼 추가 (nullable로 먼저 추가)
ALTER TABLE challenge
    ADD COLUMN newsletter_group_id BIGINT NULL;
