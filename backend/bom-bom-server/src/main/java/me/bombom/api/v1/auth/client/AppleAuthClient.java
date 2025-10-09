package me.bombom.api.v1.auth.client;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.AppleClientSecretGenerator;
import me.bombom.api.v1.auth.client.dto.AppleNativeTokenResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
@RequiredArgsConstructor
public class AppleAuthClient {

    private static final String APPLE_BASE = "https://appleid.apple.com";
    private static final String TOKEN_URI = APPLE_BASE + "/auth/token";
    private static final String REVOKE_URI = APPLE_BASE + "/auth/revoke";

    private final RestClient.Builder restClientBuilder;
    private final AppleClientSecretGenerator clientSecretGenerator;

    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        ClientRegistration reg = request.getClientRegistration();
        String clientId = reg.getClientId();
        String code = getAuthorizationCode(request);
        String redirectUri = getRedirectUri(request);

        Map<String, Object> response = requestToken(clientId, code, redirectUri);
        return toAccessTokenResponse(response, reg);
    }

    public AppleNativeTokenResponse getTokenResponse(String code, String clientId) {
        Map<String, Object> response = requestToken(clientId, code, null);
        return AppleNativeTokenResponse.from(response);
    }

    public void revokeToken(String accessToken, String clientId) {
        if (accessToken == null || accessToken.isBlank()) {
            log.info("AccessToken이 존재하지 않아 토큰 철회를 스킵합니다.");
            return;
        }
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("token", accessToken);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecretGenerator.generateFor(clientId));
        form.add("token_type_hint", "access_token");

        restClientBuilder.build().post()
                .uri(REVOKE_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .toBodilessEntity();
        log.info("Apple token revoke 성공");
    }

    private Map<String, Object> requestToken(String clientId, String code, String redirectUri) {
        MultiValueMap<String, String> form = buildTokenRequestBody(clientId, code, redirectUri);
        Map<String, Object> response = requestAccessToken(form);
        validateTokenResponse(response);
        return response;
    }

    private MultiValueMap<String, String> buildTokenRequestBody(
            String clientId,
            String code,
            String redirectUri
    ) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", clientId);
        form.add("client_secret", clientSecretGenerator.generateFor(clientId));
        form.add("code", code);
        form.add("grant_type", "authorization_code");
        if (redirectUri != null) {
            form.add("redirect_uri", redirectUri);
        }
        return form;
    }

    private Map<String, Object> requestAccessToken(MultiValueMap<String, String> form) {
        log.info("Apple 토큰 교환 요청 - URI: {}", TOKEN_URI);
        return restClientBuilder.build()
                .post()
                .uri(TOKEN_URI)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private void validateTokenResponse(Map<String, Object> map) {
        if (map == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token_response"), "Apple 응답이 null");
        }
        if (map.containsKey("error")) {
            String err = String.valueOf(map.get("error"));
            String desc = String.valueOf(map.getOrDefault("error_description", "No description"));
            log.error("Apple 토큰 교환 실패 - error: {}, desc: {}", err, desc);
            throw new OAuth2AuthenticationException(new OAuth2Error(err), "Apple 인증 실패: " + desc);
        }
        if (!map.containsKey("access_token")) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token_response"), "Apple 응답에 access_token 없음");
        }
    }

    private OAuth2AccessTokenResponse toAccessTokenResponse(Map<String, Object> response, ClientRegistration clientRegistration) {
        return OAuth2AccessTokenResponse.withToken(String.valueOf(response.get("access_token")))
                .tokenType(org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(Long.parseLong(String.valueOf(response.get("expires_in"))))
                .scopes(Set.of(clientRegistration.getScopes().toArray(new String[0])))
                .refreshToken(response.get("refresh_token") != null ? String.valueOf(response.get("refresh_token")) : null)
                .additionalParameters(response)
                .build();
    }

    private String getAuthorizationCode(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getAuthorizationExchange().getAuthorizationResponse().getCode();
    }

    private String getRedirectUri(OAuth2AuthorizationCodeGrantRequest request) {
        return request.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri();
    }
}
