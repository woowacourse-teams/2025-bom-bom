package me.bombom.api.v1.auth.client;

/**
 * 토큰 철회 클라이언트 인터페이스
 * 다양한 OAuth2 제공자의 토큰 철회 API를 추상화
 */
public interface RevokeClient {

    /**
     * 토큰 철회 결과
     */
    record RevokeResult(
        boolean success,
        String errorMessage
    ) {
        public static RevokeResult ofSuccess() {
            return new RevokeResult(true, null);
        }
        
        public static RevokeResult ofFailure(String errorMessage) {
            return new RevokeResult(false, errorMessage);
        }
    }

    /**
     * 토큰을 철회합니다
     * @param token 철회할 토큰
     * @param clientSecret 클라이언트 시크릿
     * @return 철회 결과
     */
    RevokeResult revoke(String token, String clientSecret);
}
