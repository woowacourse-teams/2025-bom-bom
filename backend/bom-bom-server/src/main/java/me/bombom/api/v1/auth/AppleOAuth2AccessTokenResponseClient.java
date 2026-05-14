package me.bombom.api.v1.auth;

import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
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
public class AppleOAuth2AccessTokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {

    private final Supplier<String> clientSecretSupplier;
    private final RestClient restClient;

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest authorizationGrantRequest) {
        ClientRegistration clientRegistration = authorizationGrantRequest.getClientRegistration();
        String authorizationCode = authorizationGrantRequest.getAuthorizationExchange().getAuthorizationResponse().getCode();

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("client_id", clientRegistration.getClientId());
        params.add("client_secret", clientSecretSupplier.get());
        params.add("code", authorizationCode);
        params.add("grant_type", "authorization_code");
        params.add("redirect_uri", authorizationGrantRequest.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());

        String tokenUri = clientRegistration.getProviderDetails().getTokenUri();

        try {
            log.info("Apple 토큰 교환 요청 시작 - URI: {}", tokenUri);
            Map<String, Object> responseMap = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (responseMap == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error("invalid_token_response"), "Apple로부터 받은 토큰 응답이 null입니다.");
            }

            // Apple이 에러를 반환했는지 확인
            if (responseMap.containsKey("error")) {
                String error = responseMap.get("error").toString();
                String errorDescription = responseMap.getOrDefault("error_description", "No description").toString();
                log.error("Apple 토큰 교환 실패 - error: {}, description: {}", error, errorDescription);
                throw new OAuth2AuthenticationException(new OAuth2Error(error), "Apple 인증 실패: " + errorDescription);
            }

            // 성공 응답을 OAuth2AccessTokenResponse 객체로 수동 변환
            OAuth2AccessTokenResponse tokenResponse = OAuth2AccessTokenResponse.withToken(responseMap.get("access_token").toString())
                    .tokenType(org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER)
                    .expiresIn(Long.parseLong(responseMap.get("expires_in").toString()))
                    .scopes(Set.of(clientRegistration.getScopes().toArray(new String[0])))
                    .refreshToken(responseMap.get("refresh_token") != null ? responseMap.get("refresh_token").toString() : null)
                    .additionalParameters(responseMap)
                    .build();

            // id_token은 additionalParameters에 있으므로, 여기서 access_token이 null인지 확인할 필요는 없음.
            // Spring Security의 다음 단계(OidcUserService)에서 id_token을 사용함.
            if (tokenResponse.getAccessToken() == null) {
                throw new OAuth2AuthenticationException(
                        new OAuth2Error("invalid_token_response"),
                        "Apple로부터 받은 응답에 access_token이 없습니다."
                );
            }

            log.info("Apple 토큰 교환 성공. AccessToken Type: {}", tokenResponse.getAccessToken().getTokenType().getValue());
            return tokenResponse;

        } catch (Exception ex) {
            log.error("Apple 토큰 교환 실패: {}", ex.getMessage(), ex);
            OAuth2Error oauth2Error = new OAuth2Error("invalid_token_response",
                    "Apple 토큰 엔드포인트 요청에 실패했습니다. 응답: " + ex.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
    }
}
