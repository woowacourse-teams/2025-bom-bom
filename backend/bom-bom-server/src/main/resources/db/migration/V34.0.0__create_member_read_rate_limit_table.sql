CREATE TABLE member_read_rate_limit
(
    member_id   BIGINT         NOT NULL,
    tokens      DECIMAL(5, 2)  NOT NULL DEFAULT 3.00,
    updated_at  DATETIME       NOT NULL,
    PRIMARY KEY (member_id)
);
