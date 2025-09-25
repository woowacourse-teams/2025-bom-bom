-- 기존 회원들의 MonthlyReadingRealtime 데이터 초기화
-- MonthlyReadingRealtime 테이블이 생성된 후, 기존 회원들에 대해 초기 데이터를 생성

INSERT INTO monthly_reading_realtime (member_id, current_count)
SELECT 
    m.id as member_id,
    0 as current_count
FROM member m
WHERE m.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1 
      FROM monthly_reading_realtime mrr 
      WHERE mrr.member_id = m.id
  );

-- 기존 회원들이 MonthlyReadingSnapshot에 있다면 해당 current_count를 realtime으로 복사
UPDATE monthly_reading_realtime mrr
JOIN monthly_reading_snapshot mrs ON mrr.member_id = mrs.member_id
SET mrr.current_count = mrs.current_count;
