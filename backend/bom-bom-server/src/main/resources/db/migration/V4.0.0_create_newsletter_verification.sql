CREATE TABLE newsletter_verification (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    newsletter_id BIGINT NOT NULL,
    email VARCHAR(60) NOT NULL
);
