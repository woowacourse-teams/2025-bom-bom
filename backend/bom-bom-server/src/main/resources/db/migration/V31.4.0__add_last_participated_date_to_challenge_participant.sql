ALTER TABLE challenge_participant
    ADD COLUMN last_participated_date DATE NULL;

UPDATE challenge_participant cp
JOIN (
    SELECT cdr.participant_id, MAX(cdr.date) AS last_participated_date
    FROM challenge_daily_result cdr
    WHERE cdr.status = 'COMPLETE'
    GROUP BY cdr.participant_id
) latest ON latest.participant_id = cp.id
SET cp.last_participated_date = latest.last_participated_date;
