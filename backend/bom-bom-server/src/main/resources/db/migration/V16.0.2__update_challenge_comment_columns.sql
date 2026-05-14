ALTER TABLE challenge_comment
    DROP COLUMN article_id,
    ADD COLUMN newsletter_id BIGINT NOT NULL AFTER id,
    ADD COLUMN article_title VARCHAR(255) NOT NULL;
