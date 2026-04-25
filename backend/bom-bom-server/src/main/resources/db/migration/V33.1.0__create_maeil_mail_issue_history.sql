CREATE TABLE maeil_mail_issue_history
(
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_date DATE        NOT NULL,
    member_id  BIGINT      NOT NULL,
    topic_id   BIGINT      NOT NULL,
    CONSTRAINT uk_maeil_mail_issue_history_date_member_topic UNIQUE (issue_date, member_id, topic_id)
);
