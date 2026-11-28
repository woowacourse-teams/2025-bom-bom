-- 매일 0시 연속 읽기 초기화 때 전날의 판정 결과를 남긴다.
-- 다른 테이블로 다시 추론할 수 없는 결과만 기록한다.
--   READ: 그날 연속 읽기에 반영된 읽기가 있음 (today_reading.read_count > 0, 가이드 메일 읽기 포함)
--   NO_ARTICLE: 평일인데 그날 받은 아티클이 없어 연속 읽기가 끊기지 않음
-- 보호막 사용은 continue_reading_shield_history, 주말·공휴일은 holiday 테이블로 알 수 있어 기록하지 않는다.
CREATE TABLE continue_reading_daily_result
(
    id          BIGINT      NOT NULL AUTO_INCREMENT,
    member_id   BIGINT      NOT NULL,
    result_date DATE        NOT NULL,
    result      VARCHAR(20) NOT NULL,
    created_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_continue_reading_daily_result_member_date UNIQUE (member_id, result_date),
    INDEX idx_continue_reading_daily_result_date (result_date)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;
