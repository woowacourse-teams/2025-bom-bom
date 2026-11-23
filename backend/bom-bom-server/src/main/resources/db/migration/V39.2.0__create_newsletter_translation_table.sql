CREATE TABLE newsletter_translation (
    id BIGINT NOT NULL AUTO_INCREMENT,
    newsletter_id BIGINT NOT NULL,
    language VARCHAR(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    name VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    description VARCHAR(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_newsletter_translation_newsletter_language UNIQUE (newsletter_id, language),
    CONSTRAINT fk_newsletter_translation_newsletter
        FOREIGN KEY (newsletter_id) REFERENCES newsletter (id) ON DELETE CASCADE
);
