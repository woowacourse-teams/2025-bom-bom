-- 기존 모든 챌린지에 REVIEW TODO row 추가 (마지막 날 출석 인정용)
-- uk_challenge_todo (challenge_id, todo_type) unique 제약으로 idempotent 보장
INSERT INTO challenge_todo (challenge_id, todo_type)
SELECT c.id, 'REVIEW'
FROM challenge c
WHERE NOT EXISTS (
    SELECT 1 FROM challenge_todo ct
    WHERE ct.challenge_id = c.id AND ct.todo_type = 'REVIEW'
);
