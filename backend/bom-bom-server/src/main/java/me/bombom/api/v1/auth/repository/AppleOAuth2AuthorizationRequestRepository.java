package me.bombom.api.v1.auth.repository;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Duration;
import java.util.Base64;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.List;
import me.bombom.api.v1.auth.dto.PendingAppleAuthorizationRequest;
import me.bombom.api.v1.auth.service.AppleAuthorizationRequestService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.client.web.AuthorizationRequestRepository;
import org.springframework.security.oauth2.client.web.HttpSessionOAuth2AuthorizationRequestRepository;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

@Component
public class AppleOAuth2AuthorizationRequestRepository implements AuthorizationRequestRepository<OAuth2AuthorizationRequest> {

    private static final Duration LIFETIME = Duration.ofMinutes(5);
    private static final String CALLBACK = "/login/oauth2/code/apple";
    private static final String FORM_CALLBACK = "/login/sso/verify/success/apple";
    private static final List<String> CONTEXT_ATTRIBUTES = List.of("redirectUrl", "pendingWithdraw", "withdrawMemberId");
    private final HttpSessionOAuth2AuthorizationRequestRepository delegate = new HttpSessionOAuth2AuthorizationRequestRepository();
    private final SecureRandom random = new SecureRandom();
    private final AppleAuthorizationRequestService appleAuthorizationRequestService;
    private final Clock clock;
    private final String cookieName;

    public AppleOAuth2AuthorizationRequestRepository(AppleAuthorizationRequestService appleAuthorizationRequestService, Clock clock,
            @Value("${app.auth.apple-request-cookie-name:__Host-BOMBOM_APPLE_AUTH}") String cookieName) {
        Assert.isTrue(cookieName.startsWith("__Host-"), "Apple request cookie must use __Host- prefix");
        this.appleAuthorizationRequestService = appleAuthorizationRequestService;
        this.clock = clock;
        this.cookieName = cookieName;
    }

    @Override
    public OAuth2AuthorizationRequest loadAuthorizationRequest(HttpServletRequest request) {
        if (!isAppleCallback(request)) {
            return delegate.loadAuthorizationRequest(request);
        }
        PendingAppleAuthorizationRequest pending = findPending(request);
        return pending == null ? null : pending.authorizationRequest();
    }

    @Override
    public void saveAuthorizationRequest(OAuth2AuthorizationRequest authorizationRequest,
            HttpServletRequest request, HttpServletResponse response) {
        if (authorizationRequest == null || !"apple".equals(authorizationRequest.getAttribute("registration_id"))) {
            delegate.saveAuthorizationRequest(authorizationRequest, request, response);
            return;
        }
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        HashMap<String, Serializable> context = new HashMap<>();
        HttpSession session = request.getSession(false);
        if (session != null) {
            for (String name : CONTEXT_ATTRIBUTES) {
                if (session.getAttribute(name) instanceof Serializable value) {
                    context.put(name, value);
                }
            }
        }
        appleAuthorizationRequestService.save(hash(token), new PendingAppleAuthorizationRequest(authorizationRequest, context), clock.instant().plus(LIFETIME));
        writeCookie(response, token, LIFETIME);
    }

    @Override
    public OAuth2AuthorizationRequest removeAuthorizationRequest(HttpServletRequest request, HttpServletResponse response) {
        if (!isAppleCallback(request)) {
            return delegate.removeAuthorizationRequest(request, response);
        }
        PendingAppleAuthorizationRequest pending = findPending(request);
        String token = token(request);
        if (pending == null || token == null || !appleAuthorizationRequestService.consume(hash(token), clock.instant())) {
            return null;
        }
        // 오래된 탭의 콜백이 현재 진행 중인 다른 로그인 쿠키를 삭제하지 않게 한다.
        writeCookie(response, "", Duration.ZERO);
        // 취소 응답은 새 일반 세션을 발급하지 않아 기존 로그인 쿠키를 덮어쓰지 않는다.
        if (request.getParameter("error") != null) {
            return pending.authorizationRequest();
        }
        HttpSession session = request.getSession();
        pending.sessionAttributes().forEach(session::setAttribute);
        String userJson = request.getParameter("user");
        if (userJson != null && !userJson.isBlank()) {
            session.setAttribute("appleUserParam", userJson);
        }
        return pending.authorizationRequest();
    }

    private PendingAppleAuthorizationRequest findPending(HttpServletRequest request) {
        String token = token(request);
        String state = request.getParameter("state");
        if (token == null || state == null || state.isBlank() || state.length() > 512) {
            return null;
        }
        return appleAuthorizationRequestService.find(hash(token), state, clock.instant());
    }

    private String token(HttpServletRequest request) {
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if (cookieName.equals(cookie.getName()) && cookie.getValue().matches("[A-Za-z0-9_-]{43}")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private boolean isAppleCallback(HttpServletRequest request) {
        String path = request.getServletPath();
        return CALLBACK.equals(path) || FORM_CALLBACK.equals(path);
    }

    private void writeCookie(HttpServletResponse response, String value, Duration maxAge) {
        response.addHeader(HttpHeaders.SET_COOKIE, ResponseCookie.from(cookieName, value)
                .httpOnly(true).secure(true).sameSite("None").path("/").maxAge(maxAge).build().toString());
    }

    private String hash(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256")
                    .digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }
}
