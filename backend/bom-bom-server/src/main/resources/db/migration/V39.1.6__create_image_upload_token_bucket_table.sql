-- 이미지 업로드 rate limit: 문의 이미지를 포함해 회원/비회원이 업로드하는 모든 이미지를 하나의 버킷으로 제한한다.
-- bucket_key는 '회원타입:식별값' 형식이다. 예: 'member:1', 'guest:550e8400-e29b-41d4-a716-446655440000'
-- 'guest:'(6자) + UUID(36자) = 42자가 최대 길이다.
CREATE TABLE image_upload_token_bucket (
    bucket_key VARCHAR(48) NOT NULL,
    tokens DOUBLE NOT NULL,
    updated_at DATETIME(6) NOT NULL,
    PRIMARY KEY (bucket_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
