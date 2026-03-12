package me.bombom.api.v1.common.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class LoginMemberArgumentResolverTest {

    private static final String SESSION_COOKIE_NAME = "JSESSIONID_TEST";
    private static final String SESSION_COOKIE_DOMAIN = "example.com";

    private LoginMemberArgumentResolver resolver;

    @BeforeEach
    void setUp() {
        resolver = new LoginMemberArgumentResolver(SESSION_COOKIE_NAME, SESSION_COOKIE_DOMAIN);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }


    static class StubController {
        public void endpointMember(@LoginMember Member member) {
        }

        public void endpointMemberAnonymousTrue(@LoginMember(anonymous = true) Member member) {
        }

        public void endpointLong(@LoginMember Long memberId) {
        }

        public void endpointLongAnonymousTrue(@LoginMember(anonymous = true) Long memberId) {
        }
    }

    @Test
    @DisplayName("Member 타입과 Long 타입을 모두 지원한다")
    void supportsParameter() throws Exception {
        assertSoftly(softly -> {
            softly.assertThat(resolver.supportsParameter(paramMember())).isTrue();
            softly.assertThat(resolver.supportsParameter(paramLong())).isTrue();
        });
    }

    @Test
    @DisplayName("로그인하지 않은 유저가 익명 허용 엔드포인트에 접근하면 null을 반환한다")
    void anonymousTrue_WhenNotLoggedIn_ReturnsNull() throws Exception {
        setAnonymousAuthentication();

        Object result = resolver.resolveArgument(paramMemberAnonymousTrue(), null, null, null);
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("로그인하지 않은 유저가 익명 비허용 엔드포인트에 접근하면 UNAUTHORIZED 예외를 던진다")
    void anonymousFalse_WhenNotLoggedIn_ThrowsException() throws Exception {
        setAnonymousAuthentication();

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, null, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.UNAUTHORIZED);
    }

    @Test
    @DisplayName("정상 로그인 유저에 대해 Member 객체를 반환한다")
    void resolveMember_WhenLoggedIn() throws Exception {
        Member member = mock(Member.class);
        setLoggedInAuthentication(member);

        Object result = resolver.resolveArgument(paramMember(), null, null, null);

        assertThat(result).isEqualTo(member);
    }

    @Test
    @DisplayName("정상 로그인 유저에 대해 Long memberId를 반환한다")
    void resolveLong_WhenLoggedIn() throws Exception {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        setLoggedInAuthentication(member);

        Object result = resolver.resolveArgument(paramLong(), null, null, null);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    @DisplayName("OAuth2 유저 정보가 비정상(Member null)이면 UNAUTHORIZED 예외를 던진다")
    void resolve_WhenMemberIsNullInPrincipal_ThrowsException() throws Exception {
        setLoggedInAuthentication(null);

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, null, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.UNAUTHORIZED);
    }

    @Test
    @DisplayName("principal 타입은 정상이지만 member가 null이고 세션 쿠키가 있으면 UNAUTHORIZED 예외와 함께 세션/쿠키를 정리한다")
    void resolve_WhenMemberIsNullWithSessionCookie_ClearsInvalidSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie(SESSION_COOKIE_NAME, "session-token"));
        request.getSession();
        setLoggedInAuthentication(null);

        NativeWebRequest webRequest = createWebRequest(request, response);

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, webRequest, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.UNAUTHORIZED);
        verifyInvalidSessionCookie(response);
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    @DisplayName("비정상적인 Principal 타입이면 INVALID_TOKEN 예외를 던진다")
    void resolve_WhenInvalidPrincipal_ThrowsException() throws Exception {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("notCustomUser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, null, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_TOKEN);
    }

    @Test
    @DisplayName("비로그인(anonymous) 요청에서 세션 쿠키가 있으면 세션/쿠키를 정리한다")
    void resolve_WhenAnonymousWithSessionCookie_ClearsInvalidSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie(SESSION_COOKIE_NAME, "session-token"));
        request.getSession();

        setAnonymousAuthentication();
        NativeWebRequest webRequest = createWebRequest(request, response);

        Object result = resolver.resolveArgument(paramMemberAnonymousTrue(), null, webRequest, null);

        assertThat(result).isNull();
        verifyInvalidSessionCookie(response);
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    @DisplayName("principal 타입이 비정상이고 세션 쿠키가 있으면 INVALID_TOKEN 예외와 함께 세션/쿠키를 정리한다")
    void resolve_WhenInvalidPrincipalWithSessionCookie_ClearsInvalidSession() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie(SESSION_COOKIE_NAME, "session-token"));
        request.getSession();
        TestingAuthenticationToken auth = new TestingAuthenticationToken("notCustomUser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        NativeWebRequest webRequest = createWebRequest(request, response);

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, webRequest, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_TOKEN);
        verifyInvalidSessionCookie(response);
        assertThat(request.getSession(false)).isNull();
    }

    private void setAnonymousAuthentication() {
        Authentication anonymous = new AnonymousAuthenticationToken(
                "key", "anonymousUser", List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS")));
        SecurityContextHolder.getContext().setAuthentication(anonymous);
    }

    private void setLoggedInAuthentication(Member member) {
        CustomOAuth2User user = new CustomOAuth2User(Map.of("name", "tester"), member, null, null);
        OAuth2AuthenticationToken auth = new OAuth2AuthenticationToken(
                user, user.getAuthorities(), "registrationId");
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private MethodParameter paramMember() {
        try {
            Method m = StubController.class.getMethod("endpointMember", Member.class);
            return new MethodParameter(m, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private MethodParameter paramMemberAnonymousTrue() {
        try {
            Method m = StubController.class.getMethod("endpointMemberAnonymousTrue", Member.class);
            return new MethodParameter(m, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private MethodParameter paramLong() {
        try {
            Method m = StubController.class.getMethod("endpointLong", Long.class);
            return new MethodParameter(m, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private NativeWebRequest createWebRequest(MockHttpServletRequest request, MockHttpServletResponse response) {
        NativeWebRequest webRequest = mock(NativeWebRequest.class);
        given(webRequest.getNativeRequest(HttpServletRequest.class)).willReturn(request);
        given(webRequest.getNativeResponse(HttpServletResponse.class)).willReturn(response);
        return webRequest;
    }

    private void verifyInvalidSessionCookie(MockHttpServletResponse response) {
        assertThat(response.getCookie(SESSION_COOKIE_NAME)).isNotNull();
        assertThat(response.getCookie(SESSION_COOKIE_NAME).getMaxAge()).isEqualTo(0);
        assertThat(response.getCookie(SESSION_COOKIE_NAME).getPath()).isEqualTo("/");
    }
}
