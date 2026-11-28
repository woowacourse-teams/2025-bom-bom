-- 어드민 알림 지표(BOM-1326) 기간별 발송 상태 집계용.
-- 기존 큰 테이블에 거는 인덱스라 테이블 생성(V39.3.0)과 분리해 부분 실패 시 복구 범위를 좁힌다.
-- 온라인 DDL로 만들어 인덱스 생성 중에도 알림 insert/update를 막지 않는다.
CREATE INDEX idx_article_arrival_notification_created_at
    ON article_arrival_notification (created_at) ALGORITHM=INPLACE LOCK=NONE;
CREATE INDEX idx_article_arrival_notification_failed_created_at
    ON article_arrival_notification_failed (created_at) ALGORITHM=INPLACE LOCK=NONE;
CREATE INDEX idx_challenge_start_notification_created_at
    ON challenge_start_notification (created_at) ALGORITHM=INPLACE LOCK=NONE;
CREATE INDEX idx_challenge_todo_reminder_notification_created_at
    ON challenge_todo_reminder_notification (created_at) ALGORITHM=INPLACE LOCK=NONE;
CREATE INDEX idx_inquiry_message_arrival_notification_created_at
    ON inquiry_message_arrival_notification (created_at) ALGORITHM=INPLACE LOCK=NONE;
