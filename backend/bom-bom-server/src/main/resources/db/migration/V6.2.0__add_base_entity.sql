ALTER TABLE monthly_reading_realtime
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    ADD COLUMN updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6)
        ON UPDATE CURRENT_TIMESTAMP(6);

-- 선택: member의 시간으로 백필
UPDATE monthly_reading_realtime r
    JOIN member m ON m.id = r.member_id
SET
    r.created_at = COALESCE(m.created_at, r.created_at),
    r.updated_at = COALESCE(m.updated_at, r.updated_at);
