ALTER TABLE challenge_daily_result
ADD COLUMN `status` ENUM('COMPLETE', 'SHIELD', 'NONE') NOT NULL
