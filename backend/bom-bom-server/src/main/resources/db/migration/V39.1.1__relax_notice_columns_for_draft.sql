-- 공지 초안(POST) 생성 시에는 내용이 비어 있으므로 NOT NULL 제약 완화
-- 실제 내용은 수정(PATCH) API에서 채운다
ALTER TABLE notice
    MODIFY COLUMN title varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    MODIFY COLUMN content mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    MODIFY COLUMN notice_category ENUM('NOTICE', 'UPDATE', 'EVENT', 'CHECK') NULL;
