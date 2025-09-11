package me.bombom.api.v1.auth;

import java.util.function.Supplier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.util.MultiValueMap;

public class AppleAuthRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> {

    private static final String AUTHORIZATION_CODE = "authorization_code";

    private final DefaultOAuth2TokenRequestParametersConverter delegate = new DefaultOAuth2TokenRequestParametersConverter();

    private final Supplier<String> clientSecretSupplier;

    public AppleAuthRequestEntityConverter(Supplier<String> clientSecretSupplier) {
        this.clientSecretSupplier = clientSecretSupplier;
    }

    @Override
    public MultiValueMap<String, String> convert(OAuth2AuthorizationCodeGrantRequest request) {
        System.out.println("=== Apple OAuth2 요청 파라미터 생성 ===");
        
        MultiValueMap<String, String> params = delegate.convert(request);
        
        String clientId = request.getClientRegistration().getClientId();
        String clientSecret = clientSecretSupplier.get();
        String redirectUri = request.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri();
        
        System.out.println("client_id: " + clientId);
        System.out.println("client_secret 길이: " + (clientSecret != null ? clientSecret.length() : "null"));
        System.out.println("redirect_uri: " + redirectUri);
        System.out.println("grant_type: " + AUTHORIZATION_CODE);
        
        params.set("client_id", clientId);
        params.set("client_secret", clientSecret);
        params.set("redirect_uri", redirectUri);
        params.set("grant_type", AUTHORIZATION_CODE);
        
        System.out.println("=== Apple OAuth2 요청 파라미터 완료 ===");
        return params;
    }
}
