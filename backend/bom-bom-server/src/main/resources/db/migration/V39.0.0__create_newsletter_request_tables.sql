CREATE TABLE newsletter_request
(
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    requested_name          VARCHAR(50)  NOT NULL,
    requested_url           VARCHAR(512) NOT NULL,
    normalized_url          VARCHAR(512) NOT NULL,
    requester_member_id     BIGINT       NOT NULL,
    reason                  VARCHAR(200) NULL,
    is_notification_enabled BOOLEAN      NOT NULL DEFAULT TRUE,
    status                  ENUM ('RECEIVED', 'REVIEWING', 'APPROVED', 'REJECTED') NOT NULL,
    like_count              INT          NOT NULL DEFAULT 0,
    newsletter_id           BIGINT       NULL,
    reject_reason           VARCHAR(255) NULL,
    created_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_newsletter_request_normalized_url UNIQUE (normalized_url)
);

CREATE INDEX idx_newsletter_request_status_like_count ON newsletter_request (status, like_count);
CREATE INDEX idx_newsletter_request_requester_member_id ON newsletter_request (requester_member_id);

CREATE TABLE newsletter_request_like
(
    id                    BIGINT      NOT NULL AUTO_INCREMENT,
    member_id             BIGINT      NOT NULL,
    newsletter_request_id BIGINT      NOT NULL,
    created_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at            DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_newsletter_request_like_member_request UNIQUE (member_id, newsletter_request_id)
);

CREATE INDEX idx_newsletter_request_like_newsletter_request_id ON newsletter_request_like (newsletter_request_id);

CREATE TABLE newsletter_request_draft
(
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    newsletter_request_id   BIGINT       NOT NULL,
    collect_status          ENUM ('PENDING', 'COLLECTING', 'SUCCESS', 'FAILED') NOT NULL,
    collect_attempt_count   INT          NOT NULL DEFAULT 0,
    collect_started_at      DATETIME(6)  NULL,
    failure_reason          VARCHAR(255) NULL,
    name                    VARCHAR(255) NULL,
    description             VARCHAR(255) NULL,
    image_url               VARCHAR(512) NULL,
    email                   VARCHAR(60)  NULL,
    category_id             BIGINT       NULL,
    main_page_url           VARCHAR(512) NULL,
    subscribe_url           VARCHAR(512) NULL,
    issue_cycle             VARCHAR(255) NULL,
    sender                  VARCHAR(100) NULL,
    subscribe_method        VARCHAR(512) NULL,
    previous_newsletter_url VARCHAR(512) NULL,
    created_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at              DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_newsletter_request_draft_request_id UNIQUE (newsletter_request_id)
);

CREATE INDEX idx_newsletter_request_draft_collect_status ON newsletter_request_draft (collect_status);
