package me.bombom.api.v1.common.session;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.time.Clock;
import java.time.Duration;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.session.web.http.CookieSerializer;
import org.springframework.session.web.http.CookieSerializer.CookieValue;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * 로그인된 요청이면 일정 간격으로 세션 쿠키를 같은 세션 ID로 다시 내려보낸다.
 *
 * <p>Spring Session은 세션 ID가 새로 생기거나 바뀔 때만 쿠키를 내려보낸다. 그런데 WebKit(Safari, iOS 웹뷰)은
 * www와 다른 CloudFront로 연결된 api.bombom.news 응답이 설정한 쿠키의 만료를 최대 7일로 줄인다(CNAME/IP cloaking 방어).
 * 그래서 매일 쓰는 사용자도 로그인 7일 뒤 로그아웃된다. 쿠키를 다시 받을 때마다 7일 상한이 새로 시작되므로,
 * 7일 안에 다시 쓰는 사용자는 로그인이 유지된다.
 */
public class SessionCookieRefreshInterceptor implements HandlerInterceptor {

    public static final String REFRESHED_AT_ATTRIBUTE = "sessionCookieRefreshedAt";

    private final CookieSerializer cookieSerializer;
    private final Clock clock;
    private final Duration refreshInterval;

    public SessionCookieRefreshInterceptor(
            CookieSerializer cookieSerializer,
            Clock clock,
            Duration refreshInterval
    ) {
        this.cookieSerializer = cookieSerializer;
        this.clock = clock;
        this.refreshInterval = refreshInterval;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        if (session == null || !isLoggedIn()) {
            return true;
        }

        long now = clock.millis();
        if (isRefreshDue(session, now)) {
            cookieSerializer.writeCookieValue(new CookieValue(request, response, session.getId()));
            session.setAttribute(REFRESHED_AT_ATTRIBUTE, now);
        }
        return true;
    }

    private boolean isLoggedIn() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return false;
        }
        return authentication.getPrincipal() instanceof CustomOAuth2User oauth2User
                && oauth2User.getMember() != null;
    }

    private boolean isRefreshDue(HttpSession session, long now) {
        Object refreshedAt = session.getAttribute(REFRESHED_AT_ATTRIBUTE);
        if (!(refreshedAt instanceof Long refreshedAtMillis)) {
            return true;
        }
        return now - refreshedAtMillis >= refreshInterval.toMillis();
    }
}
