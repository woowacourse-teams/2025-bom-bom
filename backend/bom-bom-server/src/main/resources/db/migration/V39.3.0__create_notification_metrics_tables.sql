-- 어드민 알림 지표(BOM-1326). 테이블은 fcm-server가 사용한다.

-- 기기 OS 알림 권한 허용 여부. NULL이면 앱이 아직 보고하지 않음
ALTER TABLE member_fcm_token
    ADD COLUMN os_permission_granted BOOLEAN NULL;

-- 푸시 알림을 눌러 앱을 연 기록. notification_id/schedule_id가 없으면 발송 건과 연결되지 않은 열림
CREATE TABLE notification_open (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(50) NULL,
    notification_id BIGINT NULL,
    schedule_id BIGINT NULL,
    member_id BIGINT NULL,
    device_uuid VARCHAR(255) NULL,
    platform VARCHAR(20) NOT NULL,
    app_version VARCHAR(50) NULL,
    opened_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_notification_open_opened_at (opened_at),
    INDEX idx_notification_open_category_opened_at (category, opened_at),
    -- 일별 추이: 발송일 코호트로 알림 row·스케줄과 열림을 잇는다
    INDEX idx_notification_open_category_notification_id (category, notification_id),
    INDEX idx_notification_open_schedule_id (schedule_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 회원별 알림 1건의 기기 단위 발송 결과
CREATE TABLE notification_device_send_log (
    id BIGINT NOT NULL AUTO_INCREMENT,
    category VARCHAR(50) NULL,
    notification_id BIGINT NULL,
    member_id BIGINT NOT NULL,
    device_uuid VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL,
    error_code VARCHAR(64) NULL,
    sent_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_notification_device_send_log_sent_at (sent_at),
    INDEX idx_notification_device_send_log_category_sent_at (category, sent_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 기간별 발송 상태 집계용
CREATE INDEX idx_article_arrival_notification_created_at ON article_arrival_notification (created_at);
CREATE INDEX idx_article_arrival_notification_failed_created_at ON article_arrival_notification_failed (created_at);
CREATE INDEX idx_challenge_start_notification_created_at ON challenge_start_notification (created_at);
CREATE INDEX idx_challenge_todo_reminder_notification_created_at ON challenge_todo_reminder_notification (created_at);
CREATE INDEX idx_inquiry_message_arrival_notification_created_at ON inquiry_message_arrival_notification (created_at);
