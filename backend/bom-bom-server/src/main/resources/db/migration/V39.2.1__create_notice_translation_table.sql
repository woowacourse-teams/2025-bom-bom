CREATE TABLE notice_translation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    notice_id BIGINT NOT NULL,
    language VARCHAR(2) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    title VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    content MEDIUMTEXT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_notice_translation_notice_language UNIQUE (notice_id, language),
    CONSTRAINT fk_notice_translation_notice
        FOREIGN KEY (notice_id) REFERENCES notice (id) ON DELETE CASCADE
);
