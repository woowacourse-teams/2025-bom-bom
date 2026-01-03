ALTER TABLE challenge_daily_result
MODIFY COLUMN `status` ENUM('COMPLETE', 'SHIELD') NOT NULL;
