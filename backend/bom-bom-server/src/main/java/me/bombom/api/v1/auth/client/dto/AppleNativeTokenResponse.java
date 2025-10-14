package me.bombom.api.v1.auth.client.dto;

import java.util.Map;

/**
 * Apple 네이티브 로그인 토큰 교환 응답 DTO
 */
public record AppleNativeTokenResponse(
    String accessToken,
    String refreshToken,
    Long expiresIn,
    String idToken,
    Map<String, Object> raw
) {

    public static AppleNativeTokenResponse from(Map<String, Object> response) {
        return new AppleNativeTokenResponse(
                String.valueOf(response.get("access_token")),
                response.get("refresh_token") != null ? String.valueOf(response.get("refresh_token")) : null,
                Long.parseLong(String.valueOf(response.get("expires_in"))),
                response.get("id_token") != null ? String.valueOf(response.get("id_token")) : null,
                response
        );
    }
}
