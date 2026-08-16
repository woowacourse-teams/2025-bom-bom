ALTER TABLE member
    ADD COLUMN apple_transfer_sub VARCHAR(255) NULL,
    ADD CONSTRAINT uk_member_apple_transfer_sub UNIQUE (apple_transfer_sub);
