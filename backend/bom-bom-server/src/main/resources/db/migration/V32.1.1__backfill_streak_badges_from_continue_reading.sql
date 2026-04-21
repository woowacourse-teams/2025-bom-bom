-- 현재 연속 읽기 일수(day_count) 기준으로 달성한 마일스톤(7, 15, 30, 50, 100, 200, 300, 400, 500)마다 스트릭 뱃지를 소급 발급한다.
-- 동일 회원·동일 마일스톤의 뱃지가 이미 존재하면 중복 삽입하지 않는다

INSERT INTO badge (
    member_id,
    badge_category,
    badge_grade,
    period_year,
    period_month,
    challenge_id,
    challenge_name,
    challenge_generation,
    streak_day_count,
    created_at,
    updated_at
)
SELECT 
    cr.member_id,
    'STREAK',
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    NULL,
    m.milestone,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
FROM continue_reading_realtime cr
JOIN (
    SELECT 7 AS milestone UNION ALL
    SELECT 15 UNION ALL
    SELECT 30 UNION ALL
    SELECT 50 UNION ALL
    SELECT 100 UNION ALL
    SELECT 200 UNION ALL
    SELECT 300 UNION ALL
    SELECT 400 UNION ALL
    SELECT 500
) m ON cr.day_count >= m.milestone
WHERE NOT EXISTS (
    SELECT 1
    FROM badge b
    WHERE b.member_id = cr.member_id
      AND b.badge_category = 'STREAK'
      AND b.streak_day_count = m.milestone
);
