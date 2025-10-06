package me.bombom.api.v1.auth;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Slf4j
@RequiredArgsConstructor
public class AppleTokenClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final AppleClientSecretGenerator clientSecretGenerator;
    private final RestClient restClient;

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        ClientRegistration clientRegistration = request.getClientRegistration();
        String clientId = clientRegistration.getClientId();
        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();
        String code = getAuthorizationCode(request);
        String redirectUri = getRedirectUri(request);

        MultiValueMap<String, String> params = buildParams(clientId, code, redirectUri);
        Map<String, Object> responseMap = requestToken(tokenUri, params);
        validateResponse(responseMap);
        OAuth2AccessTokenResponse tokenResponse = toAccessTokenResponse(responseMap, clientRegistration);

        log.info("Apple 토큰 교환 성공. AccessToken Type: {}", tokenResponse.getAccessToken().getTokenType().getValue());
        return tokenResponse;
    }

    private String getAuthorizationCode(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        return authorizationGrantRequest.getAuthorizationExchange()
                .getAuthorizationResponse()
                .getCode();
    }

    private String getRedirectUri(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        return authorizationGrantRequest.getAuthorizationExchange()
                .getAuthorizationRequest()
                .getRedirectUri();
    }

    private MultiValueMap<String, String> buildParams(String clientId, String code, String redirectUri) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientId);
        params.add("client_secret", clientSecretGenerator.generateFor(clientId)); //Apple P8(yml에 있음)으로 만듦
        params.add("code", code);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", redirectUri);
        return params;
    }

    private Map<String, Object> requestToken(String tokenUri, MultiValueMap<String, String> params) {
        log.info("Apple 토큰 교환 요청 시작 - URI: {}", tokenUri);
        return restClient.post()
                .uri(tokenUri)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(params)
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    private void validateResponse(Map<String, Object> responseMap) {
        if (responseMap == null) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token_response"), "Apple로부터 받은 토큰 응답이 null입니다.");
        }
        if (responseMap.containsKey("error")) {
            String error = responseMap.get("error").toString();
            String errorDescription = responseMap.getOrDefault("error_description", "No description").toString();
            log.error("Apple 토큰 교환 실패 - error: {}, description: {}", error, errorDescription);
            throw new OAuth2AuthenticationException(new OAuth2Error(error), "Apple 인증 실패: " + errorDescription);
        }
        if (!responseMap.containsKey("access_token")) {
            throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token_response"), "Apple 응답에 access_token 키가 없습니다.");
        }
    }

    /**
     * 응답 Map을 OAuth2AccessTokenResponse으로 변환
     */
    private OAuth2AccessTokenResponse toAccessTokenResponse(Map<String, Object> responseMap, ClientRegistration clientRegistration) {
        return OAuth2AccessTokenResponse.withToken(responseMap.get("access_token").toString())
                .tokenType(org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER)
                .expiresIn(Long.parseLong(responseMap.get("expires_in").toString()))
                .scopes(Set.of(clientRegistration.getScopes().toArray(new String[0])))
                .refreshToken(responseMap.get("refresh_token") != null ? responseMap.get("refresh_token").toString() : null)
                .additionalParameters(responseMap)
                .build();
    }
}
