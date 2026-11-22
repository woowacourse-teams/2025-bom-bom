-- BOM-1319 가입 경로 분석 뷰.
-- 적용 순서: 서버 배포 → Lambda TABLES에 member_signup_source 추가 → 배치가 core.member_signup_source를 만든 뒤 실행한다.
-- 테이블이 생기기 전에 실행하면 뷰 생성이 실패한다.

-- 1. 회원 기준표에 가입 경로를 붙인다. 답하지 않았거나 기능 배포 전에 가입한 회원은 NULL이다.
CREATE OR REPLACE VIEW `bombom-analytics.mart.member_base` AS
SELECT m.id AS member_id, m.provider, DATE(m.created_at) AS signup_date,
  DATE_TRUNC(DATE(m.created_at), WEEK(MONDAY)) AS signup_week,
  s.source AS signup_source
FROM `bombom-analytics.core.member` m
LEFT JOIN `bombom-analytics.core.member_signup_source` s ON s.member_id = m.id
WHERE m.provider != 'loadtest' AND m.created_at IS NOT NULL;

-- 2. 가입 경로별 정착 지표. 가입 14일이 지난 회원만 센다.
CREATE OR REPLACE VIEW `bombom-analytics.mart.signup_source_performance` AS
WITH rd AS (
  SELECT m.member_id,
    COUNT(DISTINCT IF(DATE_DIFF(DATE(t.read_at), m.signup_date, DAY) BETWEEN 0 AND 6, DATE(t.read_at), NULL)) AS read_days_w1,
    COUNTIF(DATE_DIFF(DATE(t.read_at), m.signup_date, DAY) BETWEEN 7 AND 13) AS reads_w2
  FROM `bombom-analytics.mart.member_base` m
  JOIN `bombom-analytics.core.article_read_history` t USING (member_id)
  GROUP BY 1
),
sb AS (
  SELECT member_id, COUNT(*) AS subscribe_count
  FROM `bombom-analytics.core.subscribe`
  GROUP BY 1
)
SELECT IFNULL(m.signup_source, 'UNANSWERED') AS signup_source,
  COUNT(*) AS signups,
  COUNTIF(IFNULL(sb.subscribe_count, 0) = 0) AS no_subscription,
  COUNTIF(rd.read_days_w1 > 0) AS read_in_7d,
  COUNTIF(rd.read_days_w1 >= 3) AS reached_3days,
  COUNTIF(rd.reads_w2 > 0) AS read_in_week2,
  ROUND(COUNTIF(IFNULL(sb.subscribe_count, 0) = 0) / COUNT(*), 4) AS no_subscription_rate,
  ROUND(COUNTIF(rd.read_days_w1 > 0) / COUNT(*), 4) AS read_in_7d_rate,
  ROUND(COUNTIF(rd.read_days_w1 >= 3) / COUNT(*), 4) AS reached_3days_rate,
  ROUND(COUNTIF(rd.reads_w2 > 0) / COUNT(*), 4) AS read_in_week2_rate
FROM `bombom-analytics.mart.member_base` m
LEFT JOIN rd USING (member_id)
LEFT JOIN sb USING (member_id)
WHERE m.signup_date >= '2026-11-24'
  AND DATE_ADD(m.signup_date, INTERVAL 14 DAY) <= CURRENT_DATE('Asia/Seoul')
GROUP BY 1;

-- 3. GA4 웹 이벤트로 본 모달 단계별 인원. event_category = 'SignupSource' (web/src/utils/trackSignupSourceEvent.ts).
-- 추천 구독 클릭의 event_label은 newsletter_id다.
CREATE OR REPLACE VIEW `bombom-analytics.mart.signup_modal_funnel` AS
SELECT PARSE_DATE('%Y%m%d', event_date) AS event_date,
  event_name,
  (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'event_label') AS event_label,
  COUNT(*) AS events,
  COUNT(DISTINCT user_id) AS members
FROM `bombom-analytics.analytics_509165076.events_*`
WHERE (SELECT value.string_value FROM UNNEST(event_params) WHERE key = 'event_category') = 'SignupSource'
GROUP BY 1, 2, 3;
