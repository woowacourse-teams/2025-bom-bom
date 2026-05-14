ALTER TABLE challenge_todo_reminder_notification
    ADD COLUMN phase VARCHAR(255) NOT NULL DEFAULT 'FIRST' AFTER challenge_name;
