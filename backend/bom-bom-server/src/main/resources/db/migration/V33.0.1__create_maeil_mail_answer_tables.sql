CREATE TABLE maeil_mail_user_answer
(
    id         BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    created_at datetime(6)           NULL,
    updated_at datetime(6)           NULL,
    content_id BIGINT                NOT NULL,
    member_id  BIGINT                NOT NULL,
    answer     TEXT                  NOT NULL,
);

CREATE TABLE maeil_mail_content_answer
(
    id         BIGINT AUTO_INCREMENT NOT NULL PRIMARY KEY,
    content_id BIGINT                NOT NULL,
    answer     MEDIUMTEXT            NOT NULL,
);
