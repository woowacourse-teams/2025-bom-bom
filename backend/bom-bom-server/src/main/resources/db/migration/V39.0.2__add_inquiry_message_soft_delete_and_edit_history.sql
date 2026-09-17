ALTER TABLE inquiry_message
    ADD COLUMN deleted_at DATETIME(6) NULL AFTER content;

CREATE TABLE inquiry_message_edit_history (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    content VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_inquiry_message_edit_history_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
