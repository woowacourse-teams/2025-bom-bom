package me.bombom.api.v1.auth.support;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SessionManager {

    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    // 세션을 보장. 없으면 생성. 있으면 리턴.
    public HttpSession ensure(HttpServletRequest request) {
        return request.getSession(true);
    }

    public Optional<HttpSession> get(HttpServletRequest request) {
        return Optional.ofNullable(request.getSession(false));
    }

    public void setAuth(HttpServletRequest request, Authentication authentication) {
        HttpSession session = ensure(request);
        // 쓰레드 로컬이라 데이터가 남아있을 수 있어, 새로 만들고 채우는게 안전
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        // 세션에 인증 정보 저장 (다음 요청에서도 로그인 상태 유지)
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    public void clearAuth(HttpServletRequest request) {
        get(request).ifPresent(HttpSession::invalidate);
        SecurityContextHolder.clearContext();
    }

    public void setAttribute(HttpServletRequest request, String key, Object value) {
        ensure(request).setAttribute(key, value);
    }

    public Object getAttribute(HttpServletRequest request, String key) {
        return get(request).map(s -> s.getAttribute(key))
                .orElse(null);
    }

    public void removeAttribute(HttpServletRequest request, String key) {
        get(request).ifPresent(s -> s.removeAttribute(key));
    }
}
