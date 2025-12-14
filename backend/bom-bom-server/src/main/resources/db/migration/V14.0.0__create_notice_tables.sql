-- Notice 카테고리 및 공지 테이블 생성
CREATE TABLE notice_category (
    id bigint NOT NULL AUTO_INCREMENT,
    name varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_notice_category_name` (`name`)
);

CREATE TABLE notice (
    id bigint NOT NULL AUTO_INCREMENT,
    title varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    content mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    notice_category_id bigint NOT NULL,
    created_at datetime(6) DEFAULT NULL,
    updated_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
);
