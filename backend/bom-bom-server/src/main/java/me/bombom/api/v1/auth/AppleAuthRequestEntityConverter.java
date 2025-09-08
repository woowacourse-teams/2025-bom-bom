package me.bombom.api.v1.auth;

import java.util.function.Supplier;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.oauth2.client.endpoint.DefaultOAuth2TokenRequestParametersConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.util.MultiValueMap;

public class AppleAuthRequestEntityConverter implements Converter<OAuth2AuthorizationCodeGrantRequest, MultiValueMap<String, String>> {

    private static final String AUTHORIZATION_CODE = "authorization_code";

    private final DefaultOAuth2TokenRequestParametersConverter delegate =
            new DefaultOAuth2TokenRequestParametersConverter();

    private final Supplier<String> clientSecretSupplier;

    public AppleAuthRequestEntityConverter(Supplier<String> clientSecretSupplier) {
        this.clientSecretSupplier = clientSecretSupplier;
    }

    @Override
    public MultiValueMap<String, String> convert(OAuth2AuthorizationCodeGrantRequest request) {
        MultiValueMap<String, String> params = delegate.convert(request);
        params.set("client_id", request.getClientRegistration().getClientId());
        params.set("client_secret", clientSecretSupplier.get());
        params.set("redirect_uri", request.getAuthorizationExchange().getAuthorizationRequest().getRedirectUri());
        params.set("grant_type", AUTHORIZATION_CODE);
        return params;
    }
}
