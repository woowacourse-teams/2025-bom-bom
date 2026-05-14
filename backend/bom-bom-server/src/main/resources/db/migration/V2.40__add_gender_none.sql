-- Member 테이블 성별 정보에 NONE 추가
ALTER TABLE member MODIFY COLUMN `gender` ENUM('FEMALE', 'MALE', 'NONE');
