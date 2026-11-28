-- 어드민 알림 지표(BOM-1326). 테이블은 fcm-server가 사용한다.

-- 기기 OS 알림 권한 허용 여부. NULL이면 앱이 아직 보고하지 않음
ALTER TABLE member_fcm_token
    ADD COLUMN os_permission_granted BOOLEAN NULL;

-- 이벤트 토픽 발송 시점의 수신 동의 회원 수. 토픽 발송은 수신 대상 수를 알 수 없어 열림률 분모(근사)로 쓴다
ALTER TABLE event_notification_schedule
    ADD COLUMN consenting_member_count INT NULL;

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
