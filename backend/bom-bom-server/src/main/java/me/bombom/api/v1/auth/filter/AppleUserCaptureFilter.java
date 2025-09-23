package me.bombom.api.v1.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Apple OAuth2 웹 로그인에서 response_mode=form_post로 전달되는 user 파라미터를
 * 리다이렉트 엔드포인트 진입 시 세션에 저장하는 필터.
 */
@Slf4j
public class AppleUserCaptureFilter extends OncePerRequestFilter {

    private static final String APPLE_REGISTRATION_ID = "apple";
    private static final String DEFAULT_REDIRECT_BASE = "/login/oauth2/code/";
    public static final String SESSION_ATTR_USER_JSON = "appleUserParam";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Apple redirect endpoint (POST, form_post)
        if (isAppleRedirectPost(request)) {
            String userJson = request.getParameter("user");
            if (StringUtils.hasText(userJson)) {
                HttpSession session = request.getSession(true);
                session.setAttribute(SESSION_ATTR_USER_JSON, userJson);
                log.info("Apple user 파라미터 캡처 및 세션 저장 완료");
            } else {
                log.debug("Apple user 파라미터가 요청에 없음");
            }
        }

        filterChain.doFilter(request, response);
    }

    private boolean isAppleRedirectPost(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return false;
        }
        String path = request.getRequestURI();
        // /login/oauth2/code/apple 일치 확인
        return path != null && path.startsWith(DEFAULT_REDIRECT_BASE) && path.endsWith("/" + APPLE_REGISTRATION_ID);
    }
}


