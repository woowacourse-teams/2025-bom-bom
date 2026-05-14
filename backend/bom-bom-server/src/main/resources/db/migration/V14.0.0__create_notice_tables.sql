-- Notice 공지 테이블 생성
CREATE TABLE notice (
    id bigint NOT NULL AUTO_INCREMENT,
    title varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    content mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    notice_category ENUM('NOTICE', 'UPDATE', 'EVENT', 'CHECK') NOT NULL,
    created_at datetime(6) DEFAULT NULL,
    updated_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_notice_created_at ON notice (created_at);
