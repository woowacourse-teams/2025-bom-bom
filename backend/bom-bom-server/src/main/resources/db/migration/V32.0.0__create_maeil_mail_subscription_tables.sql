CREATE TABLE maeil_mail_subscription_track
(
    id               BIGINT     NOT NULL AUTO_INCREMENT PRIMARY KEY,
    subscribe_id     BIGINT     NOT NULL,
    member_id        BIGINT     NOT NULL,
    field            VARCHAR(3) NOT NULL,
    curriculum_index INT        NOT NULL DEFAULT 0,
    CONSTRAINT uk_maeil_mail_subscription_track_subscribe_id_field UNIQUE (subscribe_id, field)
);
