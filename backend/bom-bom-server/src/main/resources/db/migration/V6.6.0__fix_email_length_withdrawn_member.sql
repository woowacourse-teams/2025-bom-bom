-- 탈퇴한 회원에 대한 email 컬럼 길이 50으로 수정
ALTER TABLE withdrawn_member
    MODIFY COLUMN email VARCHAR(50) NOT NULL;
