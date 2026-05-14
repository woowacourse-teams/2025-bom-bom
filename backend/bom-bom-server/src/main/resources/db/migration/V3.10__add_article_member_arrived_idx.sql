/* 멤버별 아티클 조회 성능 최적화를 위한 복합 인덱스 추가
 * - member_id: 필수 조건
 * - arrived_date_time DESC: 기본 정렬 조건
 * - id: 타이브레이커
 */

CREATE INDEX idx_article_member_arrived_id
ON article (member_id, arrived_date_time DESC, id);
