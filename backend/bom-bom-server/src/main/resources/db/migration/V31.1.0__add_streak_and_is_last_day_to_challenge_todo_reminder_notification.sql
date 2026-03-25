ALTER TABLE challenge_todo_reminder_notification
    ADD COLUMN streak      INT        NOT NULL DEFAULT 0,
    ADD COLUMN is_last_day TINYINT(1) NOT NULL DEFAULT 0;
