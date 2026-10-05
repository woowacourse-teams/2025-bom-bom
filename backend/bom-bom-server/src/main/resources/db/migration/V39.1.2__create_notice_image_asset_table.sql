CREATE TABLE notice_image_asset (
                                    id BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
                                    notice_id BIGINT NOT NULL,
                                    object_key VARCHAR(500) NOT NULL,
                                    image_url VARCHAR(1000) NOT NULL,
                                    status ENUM('UPLOADED', 'ATTACHED', 'DELETE_PENDING') NOT NULL,
                                    delete_requested_at DATETIME(6) NULL,
                                    created_at DATETIME(6) NOT NULL,
                                    updated_at DATETIME(6) NOT NULL,
                                    CONSTRAINT uk_notice_image_asset_object_key UNIQUE (object_key),
                                    KEY idx_notice_image_asset_notice_status (notice_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
