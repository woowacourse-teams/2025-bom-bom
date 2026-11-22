CREATE TABLE member_signup_source
(
    id         BIGINT      NOT NULL AUTO_INCREMENT,
    member_id  BIGINT      NOT NULL,
    source     VARCHAR(30) NOT NULL,
    detail     VARCHAR(50) NULL,
    created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_member_signup_source_member_id UNIQUE (member_id)
);

CREATE TABLE signup_recommendation
(
    id            BIGINT      NOT NULL AUTO_INCREMENT,
    newsletter_id BIGINT      NOT NULL,
    display_order INT         NOT NULL,
    arrival_days  VARCHAR(30) NULL,
    created_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    PRIMARY KEY (id),
    CONSTRAINT uk_signup_recommendation_newsletter_id UNIQUE (newsletter_id)
);

-- 2026-11-22 BigQuery 기준(BOM-1319). 매일메일 제외, 최근 4주 도착 100통 이상·읽은 회원 10명 이상인 뉴스레터를
-- 읽음률 순위와 읽은 회원 수 순위의 평균으로 정렬했다. arrival_days는 최근 8주 실제 도착 요일이다.
-- 운영 DB에 없는 newsletter_id는 조회할 때 활성 뉴스레터와 맞춰 걸러진다.
INSERT INTO signup_recommendation (newsletter_id, display_order, arrival_days)
VALUES
    (53, 1, '매주 금요일'),
    (48, 2, '매주 금요일'),
    (2, 3, '매주 화요일'),
    (1, 4, '화·수·금'),
    (82, 5, '격주 화요일'),
    (32, 6, '화·금'),
    (8, 7, '가끔 월요일'),
    (51, 8, '평일'),
    (124, 9, '격주 월요일'),
    (49, 10, '월·수·금'),
    (6, 11, '매주 월요일'),
    (138, 12, '매주 목요일'),
    (102, 13, '월·수·금'),
    (31, 14, '월·수·금'),
    (107, 15, '월·수·금'),
    (26, 16, '평일'),
    (50, 17, '평일'),
    (110, 18, '매주 수요일'),
    (59, 19, '매주 목요일'),
    (84, 20, '매주 화요일'),
    (71, 21, '매주 수요일'),
    (47, 22, '월~토'),
    (103, 23, '평일'),
    (28, 24, '월·수·금'),
    (29, 25, '평일'),
    (19, 26, '매주 수요일'),
    (5, 27, '매주 수요일'),
    (108, 28, '화·목'),
    (43, 29, '격주 금요일'),
    (41, 30, '매주 월요일'),
    (80, 31, '월·수·금'),
    (117, 32, '주말'),
    (38, 33, '매주 금요일'),
    (37, 34, '매주 수요일'),
    (97, 35, '화·금');
