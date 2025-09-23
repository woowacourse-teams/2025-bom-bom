package me.bombom.api.v1.auth.resolver;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@Slf4j
public class AppleAuthorizationRequestResolver implements OAuth2AuthorizationRequestResolver {

    private static final String REGISTRATION_ID = "apple";
    private final DefaultOAuth2AuthorizationRequestResolver delegate;

    public AppleAuthorizationRequestResolver(ClientRegistrationRepository clientRegistrationRepository) {
        this.delegate = new DefaultOAuth2AuthorizationRequestResolver(clientRegistrationRepository, "/oauth2/authorization");
    }

    @Override
    public OAuth2AuthorizationRequest resolve(jakarta.servlet.http.HttpServletRequest request) {
        OAuth2AuthorizationRequest req = delegate.resolve(request);
        return customizeIfApple(req);
    }

    @Override
    public OAuth2AuthorizationRequest resolve(jakarta.servlet.http.HttpServletRequest request, String clientRegistrationId) {
        OAuth2AuthorizationRequest req = delegate.resolve(request, clientRegistrationId);
        return customizeIfApple(req);
    }

    private OAuth2AuthorizationRequest customizeIfApple(OAuth2AuthorizationRequest req) {
        if (req == null) return null;
        String registrationId = (String) req.getAttributes().get("registration_id");
        if (!REGISTRATION_ID.equals(registrationId)) {
            return req;
        }

        OAuth2AuthorizationRequest.Builder builder = OAuth2AuthorizationRequest.from(req);

        // response_type=code id_token, response_mode, prompt
        Map<String, Object> additionalParameters = new HashMap<>(req.getAdditionalParameters());
        additionalParameters.put("response_mode", "form_post");
        additionalParameters.put("prompt", "consent");
        additionalParameters.put("response_type", "code id_token");
        builder.additionalParameters(additionalParameters);

        // ensure scopes contain name, email
        Set<String> scopes = new LinkedHashSet<>(req.getScopes());
        boolean changed = false;
        if (!scopes.contains("name")) { scopes.add("name"); changed = true; }
        if (!scopes.contains("email")) { scopes.add("email"); changed = true; }
        if (changed) {
            builder.scopes(scopes);
        }

        OAuth2AuthorizationRequest customized = builder.build();
        log.info("Apple 인가 요청 커스터마이즈 적용 - scopes: {}, additional: {}", customized.getScopes(), customized.getAdditionalParameters());
        return customized;
    }
}


