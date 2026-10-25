-- 메일 서버 중복 저장 방지: 같은 회원에게 같은 Message-ID 메일이 두 번 저장되지 않게 한다.
-- 메일 서버가 7일 지난 행을 지운다. article 삭제 배치가 이 테이블을 잠그지 않도록 FK는 두지 않는다.
CREATE TABLE received_mail (
    member_id BIGINT NOT NULL,
    message_id_hash BINARY(32) NOT NULL COMMENT 'SHA-256(Message-ID)',
    article_id BIGINT NOT NULL,
    created_at DATETIME(6) NOT NULL,
    PRIMARY KEY (member_id, message_id_hash)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
