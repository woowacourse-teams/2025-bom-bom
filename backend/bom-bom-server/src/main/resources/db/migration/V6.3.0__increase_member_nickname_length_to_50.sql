-- 멤버의 nickname 길이 20, email 길이 50으로 수정

ALTER TABLE member
    MODIFY COLUMN nickname VARCHAR(20) NOT NULL;

ALTER TABLE member
    MODIFY COLUMN email VARCHAR(50) NOT NULL;
