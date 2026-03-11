ALTER TABLE challenge_participant
    ADD COLUMN streak INT NOT NULL DEFAULT 0;

-- 기존 참여자의 스트릭을 challenge_daily_result 기록 기반으로 계산하여 초기화
-- challenge_daily_result에는 주말 데이터가 없으므로 금요일→월요일 간격(3일)까지 연속으로 판단
-- 오늘 미완료 여부는 결석 체크 스케줄러가 담당하므로, 마이그레이션은 과거 기록 간 간격만 판단
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
