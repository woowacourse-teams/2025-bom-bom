-- Member profile_image_url 길이 제한 수정

ALTER TABLE member
    MODIFY COLUMN profile_image_url VARCHAR(2000) NULL;
