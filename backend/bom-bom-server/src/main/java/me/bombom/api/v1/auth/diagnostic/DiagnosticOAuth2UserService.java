package me.bombom.api.v1.auth.diagnostic;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.http.OAuth2ErrorResponseErrorHandler;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DiagnosticOAuth2UserService extends DefaultOAuth2UserService {
    private final OAuth2Diagnostics diagnostics;

    @Autowired
    public DiagnosticOAuth2UserService(OAuth2Diagnostics diagnostics) {
        this(new RestTemplate(), diagnostics);
    }

    public DiagnosticOAuth2UserService(RestTemplate restTemplate, OAuth2Diagnostics diagnostics) {
        this.diagnostics = diagnostics;
        restTemplate.setErrorHandler(new OAuth2ErrorResponseErrorHandler());
        restTemplate.getInterceptors().add(diagnostics.interceptor());
        setRestOperations(restTemplate);
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) {
        diagnostics.userInfoToken(request.getAccessToken());
        return super.loadUser(request);
    }
}
