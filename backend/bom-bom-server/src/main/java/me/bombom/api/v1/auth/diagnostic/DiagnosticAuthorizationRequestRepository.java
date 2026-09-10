package me.bombom.api.v1.auth.diagnostic;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

/** A fingerprint-only companion attribute; authentication remains owned by Spring Security. */
@RequiredArgsConstructor
public class DiagnosticAuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {
    private static final String SAVED_ATTEMPT = DiagnosticAuthorizationRequestRepository.class.getName() + ".ATTEMPT";
    private final HttpSessionOAuth2AuthorizationRequestRepository delegate = new HttpSessionOAuth2AuthorizationRequestRepository();
    private final OAuth2Diagnostics diagnostics;

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        OAuth2AuthorizationRequest result = delegate.loadAuthorizationRequest(request);
        observe(request, result, "oauth_authorization_loaded");
        return result;
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorization, HttpServletRequest request,
            HttpServletResponse response) {
        if (authorization == null) {
            removeAuthorizationRequest(request, response);
            return;
        }
        SavedAttempt previous = savedAttempt(request);
        delegate.saveAuthorizationRequest(authorization, request, response);
        try {
            String fingerprint = diagnostics.stateFingerprint(authorization.getState());
            HttpSession session = request.getSession(false);
            if (session != null && fingerprint != null) {
                session.setAttribute(SAVED_ATTEMPT, Map.of("fingerprint", fingerprint, "keyId", currentKeyId()));
            }
            diagnostics.attempt(authorization.getState());
            diagnostics.record("stage", "authorization");
            diagnostics.record("previous_attempt_id", previous == null ? null : previous.fingerprint());
            diagnostics.record("previous_fingerprint_key_id", previous == null ? null : previous.keyId());
            diagnostics.record("authorization_request_replaced", previous != null);
            diagnostics.emit("oauth_authorization_saved", false);
        } catch (RuntimeException ignored) {
            diagnostics.record("diagnostic_error", true);
        }
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        SavedAttempt previous = savedAttempt(request);
        OAuth2AuthorizationRequest result = delegate.removeAuthorizationRequest(request, response);
        diagnostics.record("authorization_request_consumed", result != null);
        observe(request, result, "oauth_authorization_consumed");
        if (result != null) {
            try {
                HttpSession session = request.getSession(false);
                if (session != null && previous != null && previous.equals(savedAttempt(request))) {
                    session.removeAttribute(SAVED_ATTEMPT);
                }
            } catch (RuntimeException ignored) {
                diagnostics.record("diagnostic_error", true);
            }
        }
        return result;
    }

    private void observe(HttpServletRequest request, OAuth2AuthorizationRequest result, String event) {
        try {
            SavedAttempt expected = savedAttempt(request);
            String actual = diagnostics.stateFingerprint(request.getParameter("state"));
            diagnostics.record("saved_attempt_present", expected != null);
            diagnostics.record("saved_attempt_id", expected == null ? null : expected.fingerprint());
            diagnostics.record("saved_fingerprint_key_id", expected == null ? null : expected.keyId());
            boolean comparable = expected != null && expected.keyId().equals(currentKeyId());
            diagnostics.record("state_matches_saved", comparable && actual != null ? expected.fingerprint().equals(actual) : null);
            diagnostics.record("authorization_request_matched", result != null);
            diagnostics.emit(event, false);
        } catch (RuntimeException ignored) {
            diagnostics.record("diagnostic_error", true);
        }
    }

    private String currentKeyId() {
        return String.valueOf(diagnostics.snapshot().get("fingerprint_key_id"));
    }

    private record SavedAttempt(String fingerprint, String keyId) {
    }

    private SavedAttempt savedAttempt(HttpServletRequest request) {
        try {
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(SAVED_ATTEMPT) instanceof Map<?, ?> value
                    && value.get("fingerprint") instanceof String fingerprint && value.get("keyId") instanceof String keyId) {
                return new SavedAttempt(fingerprint, keyId);
            }
            return null;
        } catch (RuntimeException ignored) {
            diagnostics.record("diagnostic_error", true);
            return null;
        }
    }
}
