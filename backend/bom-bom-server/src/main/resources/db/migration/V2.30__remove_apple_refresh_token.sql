-- Member 테이블에서 apple_refresh_token 컬럼 제거 (refresh token 로직 제거로 인해 불필요)
ALTER TABLE member DROP COLUMN apple_refresh_token;
