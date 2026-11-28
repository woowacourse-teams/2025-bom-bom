-- 회원별 기간 조회(member_id = ? AND read_at 범위)를 인덱스로 처리한다.
-- 최근 연속 읽기 기록, 월간 리포트 달력·카테고리 통계 조회가 함께 사용한다.
-- 서비스 중에도 쓰기를 막지 않도록 온라인 DDL로 만들고, 불가능하면 잠그지 않고 실패한다.
ALTER TABLE article_read_history
    ADD INDEX idx_article_read_history_member_read_at (member_id, read_at),
    ALGORITHM = INPLACE,
    LOCK = NONE;
