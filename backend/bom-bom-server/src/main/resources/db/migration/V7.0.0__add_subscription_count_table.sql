CREATE TABLE newsletter_subscription_count (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
    newsletter_id BIGINT NOT NULL,
    total INT NOT NULL DEFAULT 0,
    age0s INT NOT NULL DEFAULT 0,
    age10s INT NOT NULL DEFAULT 0,
    age20s INT NOT NULL DEFAULT 0,
    age30s INT NOT NULL DEFAULT 0,
    age40s INT NOT NULL DEFAULT 0,
    age50s INT NOT NULL DEFAULT 0,
    age60plus INT NOT NULL DEFAULT 0,
    UNIQUE KEY uk_newsletter_subscription_count_newsletter_id (newsletter_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

