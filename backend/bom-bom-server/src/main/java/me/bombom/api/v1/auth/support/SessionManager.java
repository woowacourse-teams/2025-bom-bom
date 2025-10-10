package me.bombom.api.v1.auth.support;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Component
public class SessionManager {

    private static final String SPRING_SECURITY_CONTEXT_KEY = "SPRING_SECURITY_CONTEXT";

    public void setAuth(Authentication authentication) {
        HttpSession session = ensure();
        // 쓰레드 로컬이라 데이터가 남아있을 수 있어, 새로 만들고 채우는게 안전
        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        SecurityContextHolder.setContext(securityContext);
        // 세션에 인증 정보 저장 (다음 요청에서도 로그인 상태 유지)
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, SecurityContextHolder.getContext());
    }

    public void clearAuth() {
        get().ifPresent(HttpSession::invalidate);
        SecurityContextHolder.clearContext();
    }

    public void setAttribute(String key, Object value) {
        ensure().setAttribute(key, value);
    }

    public <T> T getAttribute(String key, Class<T> type) {
        return get()
                .map(s -> s.getAttribute(key))
                .filter(type::isInstance)
                .map(type::cast)
                .orElse(null);
    }

    public void removeAttribute(String key) {
        get().ifPresent(s -> s.removeAttribute(key));
    }

    public boolean isEmpty() {
        Optional<HttpSession> session = get();
        return session.isEmpty();
    }

    // 세션을 보장. 없으면 생성. 있으면 리턴.
    private HttpSession ensure() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return ((ServletRequestAttributes) requestAttributes).getRequest().getSession(true);
        }
        throw new IllegalStateException("Http 요청 컨텍스트가 없습니다.");
    }

    // 없으면 예외. 있으면 리턴.
    private Optional<HttpSession> get() {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes instanceof ServletRequestAttributes) {
            return Optional.ofNullable(((ServletRequestAttributes) requestAttributes).getRequest().getSession(false));
        }
        throw new IllegalStateException("Http 요청 컨텍스트가 없습니다.");
    }
}
