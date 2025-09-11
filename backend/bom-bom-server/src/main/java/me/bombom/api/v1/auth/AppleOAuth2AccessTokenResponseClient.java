package me.bombom.api.v1.auth;

import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
            OAuth2AccessTokenResponse tokenResponse = restClient.post()
                    .uri(tokenUri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(params)
                    .retrieve()
                    .body(OAuth2AccessTokenResponse.class);

            if (tokenResponse == null) {
                throw new OAuth2AuthenticationException(new OAuth2Error("token_response_is_null"), "Apple로부터 받은 토큰 응답이 null입니다.");
            }

            log.info("Apple 토큰 교환 성공. AccessToken Type: {}", tokenResponse.getAccessToken().getTokenType().getValue());
            return tokenResponse;

        } catch (Exception ex) {
            // Apple이 보내준 실제 에러 메시지를 로그에 남깁니다. (e.g., invalid_grant, invalid_client)
            log.error("Apple 토큰 교환 실패: {}", ex.getMessage(), ex);
            OAuth2Error oauth2Error = new OAuth2Error("invalid_token_response",
                    "Apple 토큰 엔드포인트 요청에 실패했습니다. 응답: " + ex.getMessage(), null);
            throw new OAuth2AuthenticationException(oauth2Error, oauth2Error.toString(), ex);
        }
    }
}
