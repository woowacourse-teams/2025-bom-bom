ALTER TABLE challenge_participant
    ADD COLUMN streak INT NOT NULL DEFAULT 0;

-- 기존 참여자의 스트릭을 challenge_daily_result 기록 기반으로 계산하여 초기화
-- challenge_daily_result에는 주말 데이터가 없으므로 금요일→월요일 간격(3일)까지 연속으로 판단
-- DAYOFWEEK: 1=일, 2=월, 7=토
UPDATE challenge_participant cp
INNER JOIN (
    WITH consecutive_check AS (
        SELECT
            participant_id,
            date,
            ROW_NUMBER() OVER (PARTITION BY participant_id ORDER BY date DESC) AS rn,
            LAG(date)     OVER (PARTITION BY participant_id ORDER BY date DESC) AS prev_date
        FROM challenge_daily_result
    ),
    gap_flags AS (
        SELECT
            participant_id,
            rn,
            CASE
                -- 가장 최근 기록이 오늘 또는 직전 평일이 아니면 gap
                WHEN rn = 1 AND DATEDIFF(CURDATE(), date) >
                    CASE DAYOFWEEK(CURDATE())
                        WHEN 1 THEN 2   -- 일요일: 직전 평일 금요일(2일 전)
                        WHEN 2 THEN 3   -- 월요일: 직전 평일 금요일(3일 전)
                        WHEN 7 THEN 1   -- 토요일: 직전 평일 금요일(1일 전)
                        ELSE 1          -- 화~금: 직전 평일 어제(1일 전)
                    END
                THEN 1
                -- 두 기록 간 간격이 3일 초과면 gap (금→월=3일은 연속으로 허용)
                WHEN rn > 1 AND DATEDIFF(prev_date, date) > 3 THEN 1
                ELSE 0
            END AS has_gap
        FROM consecutive_check
    ),
    first_gap AS (
        SELECT participant_id, MIN(rn) AS gap_rn
        FROM gap_flags
        WHERE has_gap = 1
        GROUP BY participant_id
    )
    SELECT
        gf.participant_id,
        COALESCE(fg.gap_rn, MAX(gf.rn) + 1) - 1 AS streak
    FROM gap_flags gf
    LEFT JOIN first_gap fg ON gf.participant_id = fg.participant_id
    GROUP BY gf.participant_id, fg.gap_rn
) AS streak_data ON cp.id = streak_data.participant_id
SET cp.streak = streak_data.streak;
