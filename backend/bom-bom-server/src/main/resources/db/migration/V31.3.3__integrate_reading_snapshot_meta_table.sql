CREATE TABLE reading_snapshot_meta (
    snapshot_type VARCHAR(30) NOT NULL,
    snapshot_at DATETIME(6) NOT NULL,
    PRIMARY KEY (snapshot_type)
);

INSERT INTO reading_snapshot_meta (snapshot_type, snapshot_at)
SELECT 'MONTHLY', snapshot_at
FROM monthly_reading_snapshot_meta
WHERE id = 1;

INSERT INTO reading_snapshot_meta (snapshot_type, snapshot_at)
SELECT 'CONTINUE', snapshot_at
FROM continue_reading_snapshot_meta
WHERE id = 1;

DROP TABLE monthly_reading_snapshot_meta;
DROP TABLE continue_reading_snapshot_meta;
