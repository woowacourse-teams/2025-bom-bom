ALTER TABLE inquiry_message
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER content;
