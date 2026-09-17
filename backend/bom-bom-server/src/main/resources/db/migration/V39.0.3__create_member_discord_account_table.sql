CREATE TABLE member_discord_account (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    discord_id VARCHAR(30) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_discord_account_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
