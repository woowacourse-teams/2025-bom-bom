CREATE TABLE maeil_mail_topic
(
    id            BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    track         VARCHAR(3)  NOT NULL,
    name          VARCHAR(50) NOT NULL,
    display_order INT         NOT NULL,
    created_at    DATETIME(6) NOT NULL,
    updated_at    DATETIME(6) NOT NULL,
    CONSTRAINT uk_maeil_mail_topic_track_name  UNIQUE (track, name),
    CONSTRAINT uk_maeil_mail_topic_track_order UNIQUE (track, display_order)
);

CREATE TABLE maeil_mail_content
(
    id                 BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    topic_id           BIGINT       NOT NULL,
    display_order      INT          NOT NULL,
    title              VARCHAR(255) NOT NULL,
    content            MEDIUMTEXT   NOT NULL,
    contents_text      TEXT         NOT NULL,
    contents_summary   VARCHAR(255) NOT NULL,
    expected_read_time TINYINT      NOT NULL,
    created_at         DATETIME(6)  NOT NULL,
    updated_at         DATETIME(6)  NOT NULL,
    CONSTRAINT uk_maeil_mail_content_topic_order UNIQUE (topic_id, display_order)
);

CREATE TABLE maeil_mail_topic_progress
(
    id               BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id        BIGINT      NOT NULL,
    topic_id         BIGINT      NOT NULL,
    completed_cycles INT         NOT NULL DEFAULT 0,
    created_at       DATETIME(6) NOT NULL,
    updated_at       DATETIME(6) NOT NULL,
    CONSTRAINT uk_maeil_mail_topic_progress UNIQUE (member_id, topic_id)
);

CREATE TABLE maeil_mail_sent_content
(
    id         BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    member_id  BIGINT      NOT NULL,
    topic_id   BIGINT      NOT NULL,
    content_id BIGINT      NOT NULL,
    sent_at    DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    CONSTRAINT uk_maeil_mail_sent_content UNIQUE (member_id, topic_id, content_id)
);
