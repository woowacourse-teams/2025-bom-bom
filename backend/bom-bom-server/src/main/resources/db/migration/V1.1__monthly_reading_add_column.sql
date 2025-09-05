-- monthly_reading 테이블에 rank와 next_rank_difference 컬럼 추가

ALTER TABLE monthly_reading
    ADD COLUMN rank BIGINT NOT NULL DEFAULT 0;

ALTER TABLE monthly_reading
    ADD COLUMN next_rank_difference SMALLINT NOT NULL DEFAULT 0;

-- 적용 후 즉시 값 반영
UPDATE monthly_reading mr
JOIN (
    SELECT
        member_id,
        RANK() OVER (ORDER BY current_count DESC) AS rnk,
        COALESCE((
            SELECT MIN(x.current_count)
            FROM monthly_reading x
            WHERE x.current_count > mr2.current_count) - mr2.current_count, 0) AS gap
    FROM monthly_reading mr2
    ) c
ON c.member_id = mr.member_id
SET mr.rank = c.rnk, mr.next_rank_difference = c.gap;
WHERE mr.rank IS NULL;
