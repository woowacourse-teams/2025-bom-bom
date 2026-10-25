package me.bombom.api.v1.auth.dto;

import java.io.Serializable;
import java.util.Map;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

public record PendingAppleAuthorizationRequest(
        OAuth2AuthorizationRequest authorizationRequest,
        Map<String, Serializable> sessionAttributes
) implements Serializable {
}
