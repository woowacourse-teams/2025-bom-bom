CREATE TABLE monthly_reading_snapshot_meta (
          id bigint NOT NULL  PRIMARY KEY,
          snapshot_at datetime(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


INSERT INTO monthly_reading_snapshot_meta (id, snapshot_at)
VALUES (1, NOW())
ON DUPLICATE KEY UPDATE snapshot_at = VALUES(snapshot_at);
