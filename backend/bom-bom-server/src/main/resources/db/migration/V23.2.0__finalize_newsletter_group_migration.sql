-- challenge.newsletter_group_id를 NOT NULL로 변경
ALTER TABLE challenge
    MODIFY COLUMN newsletter_group_id BIGINT NOT NULL;

-- challenge_newsletter 테이블 삭제
DROP TABLE challenge_newsletter;
