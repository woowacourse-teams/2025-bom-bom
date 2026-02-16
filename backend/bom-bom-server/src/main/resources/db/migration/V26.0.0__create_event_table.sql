CREATE TABLE `event` (
    `id`         BIGINT       NOT NULL AUTO_INCREMENT,
    `name`       VARCHAR(255) NOT NULL,
    `start_time` DATETIME(6)  NOT NULL,
    `status`     VARCHAR(32)  NOT NULL, -- SCHEDULED / IN_PROGRESS / COMPLETED / CANCELLED
    `created_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at` DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `event_notification_schedule` (
    `id`             BIGINT      NOT NULL AUTO_INCREMENT,
    `event_id`       BIGINT      NOT NULL,
    `scheduled_at`   DATETIME(6) NOT NULL,
    `type`           VARCHAR(32) NOT NULL, -- BEFORE_MINUTES / AT_START
    `minutes_before` INT         DEFAULT NULL, -- BEFORE_MINUTES일 때만 사용
    `sent`           BOOLEAN     NOT NULL DEFAULT FALSE,
    `sent_at`        DATETIME(6) DEFAULT NULL,
    `created_at`     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `updated_at`     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
