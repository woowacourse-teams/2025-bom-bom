package me.bombom.api.v1.auth.diagnostic;

import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

public class DiagnosticOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuth2Diagnostics diagnostics;

    public DiagnosticOAuth2UserService(RestTemplate restTemplate, OAuth2Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        restTemplate.getInterceptors().add(diagnostics.interceptor("userinfo"));
        setRestOperations(restTemplate);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        try {
            OAuth2User user = super.loadUser(request);
            diagnostics.emit("oauth_userinfo_succeeded", false);
            return user;
        } catch (OAuth2AuthenticationException failure) {
            diagnostics.failureDetails(failure);
            throw failure;
        }
    }
}
