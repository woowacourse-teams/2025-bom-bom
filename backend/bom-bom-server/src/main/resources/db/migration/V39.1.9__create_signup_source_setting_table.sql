-- 가입 경로 질문 대상 기준 시각(BOM-1319). 이 마이그레이션이 적용된 시각 이후에 가입한 회원에게만 묻는다.
-- member.created_at이 한국 시각으로 저장되므로 DB 시간대와 관계없이 한국 시각으로 넣는다.
CREATE TABLE signup_source_setting
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    ask_since  DATETIME(6) NOT NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id)
);

INSERT INTO signup_source_setting (ask_since)
VALUES (CONVERT_TZ(UTC_TIMESTAMP(6), '+00:00', '+09:00'));
