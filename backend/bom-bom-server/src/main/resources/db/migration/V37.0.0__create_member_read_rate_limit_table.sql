CREATE TABLE member_read_token_bucket
(
    member_id   BIGINT         NOT NULL,
    tokens      DOUBLE         NOT NULL,
    updated_at  DATETIME       NOT NULL,
    PRIMARY KEY (member_id)
);
