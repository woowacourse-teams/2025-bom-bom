CREATE TABLE maeil_mail_subscription
(
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    subscribe_id BIGINT      NOT NULL,
    member_id   BIGINT      NOT NULL,
    weekly_issue_count TINYINT NOT NULL,
    created_at  DATETIME(6),
    updated_at  DATETIME(6),
    CONSTRAINT uk_native_newsletter_subscription_subscribe_id UNIQUE (subscribe_id)
);

CREATE TABLE maeil_mail_subscription_track
(
    id                         BIGINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    maeil_mail_subscription_id BIGINT     NOT NULL,
    field                      VARCHAR(3) NOT NULL,
    curriculum_index           INT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_maeil_mail_subscription_track_subscription_id_field UNIQUE (maeil_mail_subscription_id, field)
);
