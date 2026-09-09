CREATE TABLE inquiry_category (
    id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(10) NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inquiry_room (
    id BIGINT NOT NULL AUTO_INCREMENT,
    member_id BIGINT NULL,
    guest_id VARCHAR(36) NULL,
    category_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    assignee_id BIGINT NULL,
    last_read_message_id_by_user BIGINT NULL,
    last_read_message_id_by_admin BIGINT NULL,
    closed_at DATETIME(6) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_inquiry_room_member_id (member_id),
    INDEX idx_inquiry_room_guest_id (guest_id),
    CONSTRAINT chk_inquiry_room_owner CHECK (
        (member_id IS NOT NULL AND guest_id IS NULL) OR
        (member_id IS NULL AND guest_id IS NOT NULL)
    )
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inquiry_message (
    id BIGINT NOT NULL AUTO_INCREMENT,
    room_id BIGINT NOT NULL,
    sender_type VARCHAR(10) NOT NULL,
    admin_id BIGINT NULL,
    content VARCHAR(500) NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_inquiry_message_room_id (room_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE inquiry_message_image (
    id BIGINT NOT NULL AUTO_INCREMENT,
    message_id BIGINT NOT NULL,
    image_url VARCHAR(512) NOT NULL,
    sort_order INT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (id),
    INDEX idx_inquiry_message_image_message_id (message_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
