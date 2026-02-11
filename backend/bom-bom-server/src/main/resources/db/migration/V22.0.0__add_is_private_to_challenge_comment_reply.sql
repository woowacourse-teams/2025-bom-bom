ALTER TABLE challenge_comment_reply
    ADD COLUMN is_private TINYINT(1) NOT NULL DEFAULT 0 AFTER reply;
