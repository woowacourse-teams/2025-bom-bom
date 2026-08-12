CREATE TABLE faq (
    id bigint NOT NULL AUTO_INCREMENT,
    question varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    answer mediumtext CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
    faq_category ENUM('MEMBER', 'NEWSLETTER', 'CHALLENGE', 'ETC') NOT NULL,
    created_at datetime(6) DEFAULT NULL,
    updated_at datetime(6) DEFAULT NULL,
    PRIMARY KEY (`id`)
);

CREATE INDEX idx_faq_created_at ON faq (created_at);
