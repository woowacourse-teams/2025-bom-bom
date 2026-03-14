package me.bombom.api.v1.common.resolver;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

@Slf4j
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    private static final String SESSION_COOKIE_PATH = "/";
    private static final String SESSION_DOMAIN_ATTRIBUTE_PREFIX = "; Domain=";
    private static final String SESSION_COOKIE_SECURE_FLAGS = "; Secure; HttpOnly; SameSite=None";
    private static final String SESSION_COOKIE_HEADER_FORMAT = "%s=; Max-Age=0; Path=%s%s" + SESSION_COOKIE_SECURE_FLAGS;

    private final String sessionCookieName;
    private final String sessionCookieDomain;

    public LoginMemberArgumentResolver(String sessionCookieName, String sessionCookieDomain) {
        this.sessionCookieName = sessionCookieName;
        this.sessionCookieDomain = sessionCookieDomain;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(LoginMember.class)
                && (parameter.getParameterType().equals(Member.class) || parameter.getParameterType().equals(Long.class));
    }

    @Override
    public Object resolveArgument(
            MethodParameter parameter,
            ModelAndViewContainer mavContainer,
            NativeWebRequest webRequest,
            WebDataBinderFactory binderFactory
    ) {
        LoginMember loginMember = parameter.getParameterAnnotation(LoginMember.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (isAnonymous(authentication)) {
            return resolveAnonymousRequest(loginMember, webRequest);
        }

        return resolveAuthenticatedRequest(parameter, loginMember, authentication, webRequest);
    }

    private Object resolveAnonymousRequest(LoginMember loginMember, NativeWebRequest webRequest) {
        clearInvalidSessionIfPresent(webRequest);

        if (loginMember != null && loginMember.anonymous()) {
            return null;
        }

        throw new UnauthorizedException(ErrorDetail.UNAUTHORIZED);
    }

    private Object resolveAuthenticatedRequest(
            MethodParameter parameter,
            LoginMember loginMember,
            Authentication authentication,
            NativeWebRequest webRequest
    ) {
        if (!(authentication.getPrincipal() instanceof CustomOAuth2User oauth2User)) {
            return handleInvalidAuthentication(loginMember, webRequest, ErrorDetail.INVALID_TOKEN);
        }

        Member member = oauth2User.getMember();
        if (member == null) {
            return handleInvalidAuthentication(loginMember, webRequest, ErrorDetail.UNAUTHORIZED);
        }

        if (parameter.getParameterType().equals(Long.class)) {
            return member.getId();
        }
        return member;
    }

    private boolean isAnonymous(Authentication authentication) {
        return authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken;
    }

    private Object handleInvalidAuthentication(
            LoginMember loginMember,
            NativeWebRequest webRequest,
            ErrorDetail errorDetail
    ) {
        clearInvalidSessionIfPresent(webRequest);
        if (isInvalidTokenAllowed(loginMember)) {
            return null;
        }
        throw new UnauthorizedException(errorDetail);
    }

    private boolean isInvalidTokenAllowed(LoginMember loginMember) {
        return loginMember != null && loginMember.allowInvalidToken();
    }

    private void clearInvalidSessionIfPresent(NativeWebRequest webRequest) {
        if (webRequest == null) {
            return;
        }

        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
        if (!hasSessionRequestContext(request, response)) {
            return;
        }

        invalidateSessionIfPresent(request);
        SecurityContextHolder.clearContext();
        expireSessionCookie(response);
    }

    private boolean hasSessionRequestContext(HttpServletRequest request, HttpServletResponse response) {
        return request != null
                && response != null
                && hasSessionCookie(request);
    }

    private void invalidateSessionIfPresent(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
    }

    private boolean hasSessionCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return false;
        }

        for (Cookie cookie : cookies) {
            if (isSessionCookie(cookie)) {
                return true;
            }
        }
        return false;
    }

    private boolean isSessionCookie(Cookie cookie) {
        return sessionCookieName.equals(cookie.getName()) && StringUtils.hasText(cookie.getValue());
    }

    private void expireSessionCookie(HttpServletResponse response) {
        response.addHeader("Set-Cookie", buildExpiredSessionCookieHeader());
    }

    private String buildExpiredSessionCookieHeader() {
        String domainAttribute = StringUtils.hasText(sessionCookieDomain)
                ? SESSION_DOMAIN_ATTRIBUTE_PREFIX + sessionCookieDomain
                : "";
        return String.format(SESSION_COOKIE_HEADER_FORMAT, sessionCookieName, SESSION_COOKIE_PATH, domainAttribute);
    }
}
