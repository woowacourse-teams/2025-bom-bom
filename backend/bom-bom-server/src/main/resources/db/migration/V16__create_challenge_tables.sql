CREATE TABLE challenge
(
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    name       VARCHAR(255) NOT NULL,
    generation INT          NOT NULL,
    start_date DATE         NOT NULL,
    end_date   DATE         NOT NULL,
    total_days INT          NOT NULL,
    created_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_newsletter
(
    id            BIGINT NOT NULL AUTO_INCREMENT,
    challenge_id  BIGINT NOT NULL,
    newsletter_id BIGINT NOT NULL,
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_participant
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    challenge_id      BIGINT      NOT NULL,
    member_id         BIGINT      NOT NULL,
    challenge_team_id BIGINT      NULL,
    completed_days    INT         NOT NULL DEFAULT 0,
    is_survived       TINYINT(1)  NOT NULL DEFAULT 1,
    shield            INT         NOT NULL DEFAULT 0,
    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_todo
(
    id           BIGINT                  NOT NULL AUTO_INCREMENT,
    challenge_id BIGINT                  NOT NULL,
    todo_type    ENUM ('READ','COMMENT') NOT NULL,
    created_at   DATETIME(6)             NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6)             NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_daily_todo
(
    id                BIGINT      NOT NULL AUTO_INCREMENT,
    participant_id    BIGINT      NOT NULL,
    todo_date         DATE        NOT NULL,
    challenge_todo_id BIGINT      NOT NULL,
    created_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at        DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_daily_result
(
    id             BIGINT      NOT NULL AUTO_INCREMENT,
    participant_id BIGINT      NOT NULL,
    date           DATE        NOT NULL,
    created_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_team
(
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    challenge_id BIGINT      NOT NULL,
    progress     BIGINT      NOT NULL,
    created_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at   DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE TABLE challenge_comment
(
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    article_id     BIGINT       NOT NULL,
    participant_id BIGINT       NOT NULL,
    comment        VARCHAR(255) NOT NULL,
    created_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at     DATETIME(6)  NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
