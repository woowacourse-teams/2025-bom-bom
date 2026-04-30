ALTER TABLE maeil_mail_subscription_track
    ADD COLUMN last_issued_date DATE NULL;

CREATE TABLE maeil_mail_topic
(
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    track         VARCHAR(3)  NOT NULL,
    name          VARCHAR(50) NOT NULL,
    display_order INT         NOT NULL,
    created_at    DATETIME(6) NULL,
    updated_at    DATETIME(6) NULL,
    CONSTRAINT uk_maeil_mail_topic_track_name UNIQUE (track, name),
    CONSTRAINT uk_maeil_mail_topic_track_order UNIQUE (track, display_order)
);

CREATE TABLE maeil_mail_content
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    topic_id           BIGINT       NOT NULL,
    title              VARCHAR(50)  NOT NULL,
    content            MEDIUMTEXT   NOT NULL,
    contents_text      TEXT         NOT NULL,
    contents_summary   VARCHAR(100) NOT NULL,
    expected_read_time TINYINT      NOT NULL,
    created_at         DATETIME(6)  NULL,
    updated_at         DATETIME(6)  NULL
);

CREATE TABLE maeil_mail_sent_content
(
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    topic_id   BIGINT      NOT NULL,
    content_id BIGINT      NOT NULL,
    created_at DATETIME(6) NULL,
    updated_at DATETIME(6) NULL,
    CONSTRAINT uk_maeil_mail_sent_content UNIQUE (member_id, topic_id, content_id)
);

CREATE TABLE maeil_mail_issue_history
(
    id         BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    article_id BIGINT NOT NULL,
    content_id BIGINT NOT NULL,
    CONSTRAINT uk_maeil_mail_issue_history_article_id UNIQUE (article_id)
);

CREATE TABLE maeil_mail_issue_job
(
    id                            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    issue_date                    DATE          NOT NULL,
    status                        VARCHAR(20)   NOT NULL,
    last_processed_track_id       BIGINT        NOT NULL,
    chunk_count                   BIGINT        NOT NULL,
    processed_track_count         BIGINT        NOT NULL,
    issued_article_count          BIGINT        NOT NULL,
    previously_issued_track_count BIGINT        NOT NULL,
    failed_message                VARCHAR(1000) NULL,
    started_at                    DATETIME(6)   NOT NULL,
    completed_at                  DATETIME(6)   NULL,
    failed_at                     DATETIME(6)   NULL,
    created_at                    DATETIME(6)   NULL,
    updated_at                    DATETIME(6)   NULL,
    CONSTRAINT uk_maeil_mail_issue_job_issue_date UNIQUE (issue_date)
);
