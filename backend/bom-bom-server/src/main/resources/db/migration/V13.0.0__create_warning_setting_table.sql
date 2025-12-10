CREATE TABLE warning_setting
(
    id         BIGINT NOT NULL AUTO_INCREMENT,
    member_id  BIGINT NOT NULL,
    is_visible BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    UNIQUE KEY uk_warning_setting_member_id (member_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO warning_setting (member_id, is_visible)
SELECT m.id, TRUE
FROM member m
WHERE NOT EXISTS (
    SELECT 1
    FROM warning_setting ws
    WHERE ws.member_id = m.id
);
