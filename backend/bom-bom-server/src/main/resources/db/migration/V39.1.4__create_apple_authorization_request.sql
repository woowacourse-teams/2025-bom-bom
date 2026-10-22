CREATE TABLE apple_authorization_request (
    request_key CHAR(64) CHARACTER SET ascii COLLATE ascii_bin PRIMARY KEY,
    state VARCHAR(512) CHARACTER SET ascii COLLATE ascii_bin NOT NULL,
    request_bytes MEDIUMBLOB NOT NULL,
    expires_at TIMESTAMP(6) NOT NULL,
    INDEX idx_apple_authorization_request_expiry (expires_at)
);
