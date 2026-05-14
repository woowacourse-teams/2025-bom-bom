-- Member 테이블에 apple_refresh_token 컬럼 추가 (Apple 로그인 토큰 철회용)
ALTER TABLE member ADD COLUMN apple_refresh_token VARCHAR(1000);
