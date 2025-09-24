-- 탈퇴한 회원 테이블 생성
CREATE TABLE withdrawn_member (
                                  id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                  member_id BIGINT NOT NULL,
                                  email VARCHAR(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                  birth_date DATE DEFAULT NULL,
                                  gender enum('FEMALE', 'MALE', 'NONE') CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
                                  joined_date DATE NOT NULL,
                                  deleted_date DATE NOT NULL,
                                  expire_date DATE NOT NULL,
                                  continue_reading INT NOT NULL,
                                  bookmarked_count INT NOT NULL,
                                  highlight_count INT NOT NULL
                                  PRIMARY KEY (`id`)
);
