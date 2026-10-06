ALTER TABLE inquiry_message_arrival_notification
    DROP INDEX idx_inquiry_message_arrival_notification_status,
    DROP COLUMN content,
    ADD COLUMN message_id BIGINT NOT NULL AFTER room_id;
