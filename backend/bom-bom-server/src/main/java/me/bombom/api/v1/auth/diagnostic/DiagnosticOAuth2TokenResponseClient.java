package me.bombom.api.v1.auth.diagnostic;

import org.springframework.http.converter.FormHttpMessageConverter;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.RestClientAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.web.client.RestClient;

public class DiagnosticOAuth2TokenResponseClient implements OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> {
    private final RestClientAuthorizationCodeTokenResponseClient delegate;
    private final OAuth2Diagnostics diagnostics;

    public DiagnosticOAuth2TokenResponseClient(RestClient.Builder builder, OAuth2Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
        delegate = new RestClientAuthorizationCodeTokenResponseClient();
        delegate.setRestClient(builder.messageConverters(converters -> {
            converters.clear();
            converters.add(new FormHttpMessageConverter());
            converters.add(new OAuth2AccessTokenResponseHttpMessageConverter());
        }).defaultStatusHandler(new OAuth2ErrorResponseErrorHandler())
                .requestInterceptor(diagnostics.interceptor("token_exchange")).build());
    }

    @Override
    public OAuth2AccessTokenResponse getTokenResponse(OAuth2AuthorizationCodeGrantRequest request) {
        diagnostics.attempt(request.getAuthorizationExchange().getAuthorizationRequest().getState());
        try {
            OAuth2AccessTokenResponse response = delegate.getTokenResponse(request);
            diagnostics.tokenReceived(response);
            return response;
        } catch (OAuth2AuthorizationException failure) {
            diagnostics.record("token_exchange_success", false);
            diagnostics.failureDetails(failure);
            throw failure;
        }
    }
}
