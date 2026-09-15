-- 대표 공지 1건 보장
CREATE TABLE notice_representative (
    id TINYINT NOT NULL,
    notice_id BIGINT NOT NULL,
    created_at DATETIME(6) DEFAULT NULL,
    updated_at DATETIME(6) DEFAULT NULL,
    PRIMARY KEY (id),
    CONSTRAINT chk_notice_representative_singleton CHECK (id = 1),
    CONSTRAINT fk_notice_representative_notice
        FOREIGN KEY (notice_id) REFERENCES notice (id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
