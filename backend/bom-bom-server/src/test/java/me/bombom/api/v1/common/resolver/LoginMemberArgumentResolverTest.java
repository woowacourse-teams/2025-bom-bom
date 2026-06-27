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
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.exception.UnauthorizedException;
import me.bombom.api.v1.member.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
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

        public void endpointMemberAnonymousTrueAllowInvalidToken(
                @LoginMember(anonymous = true, allowInvalidToken = true) Member member
        ) {
        }

        public void endpointMemberAllowInvalidTokenOnly(@LoginMember(allowInvalidToken = true) Member member) {
        }

        public void endpointLong(@LoginMember Long memberId) {
        }

        public void endpointLongAnonymousTrue(@LoginMember(anonymous = true) Long memberId) {
        }

        public void endpointLongAnonymousTrueAllowInvalidToken(
                @LoginMember(anonymous = true, allowInvalidToken = true) Long memberId
        ) {
        }
    }

    @Test
    void Member와_Long_타입을_모두_지원한다() throws Exception {
        assertSoftly(softly -> {
            softly.assertThat(resolver.supportsParameter(paramMember())).isTrue();
            softly.assertThat(resolver.supportsParameter(paramLong())).isTrue();
        });
    }

    @Test
    void 비로그인_사용자가_익명_허용_엔드포인트에_접근하면_null을_반환한다() throws Exception {
        setAnonymousAuthentication();

        Object result = resolver.resolveArgument(paramMemberAnonymousTrue(), null, null, null);
        assertThat(result).isNull();
    }

    @Test
    void 비로그인_사용자가_익명_비허용_엔드포인트에_접근하면_UNAUTHORIZED_예외를_던진다() throws Exception {
        setAnonymousAuthentication();

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, null, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.UNAUTHORIZED);
    }

    @Test
    void 로그인_사용자의_Member_객체를_반환한다() throws Exception {
        Member member = mock(Member.class);
        setLoggedInAuthentication(member);

        Object result = resolver.resolveArgument(paramMember(), null, null, null);

        assertThat(result).isEqualTo(member);
    }

    @Test
    void 로그인_사용자의_Long_memberId를_반환한다() throws Exception {
        Member member = mock(Member.class);
        given(member.getId()).willReturn(1L);
        setLoggedInAuthentication(member);

        Object result = resolver.resolveArgument(paramLong(), null, null, null);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void OAuth2_사용자의_Member가_null이면_UNAUTHORIZED_예외를_던진다() throws Exception {
        setLoggedInAuthentication(null);

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, null, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.UNAUTHORIZED);
    }

    @Test
    void Member가_null이고_세션_쿠키가_있으면_UNAUTHORIZED_예외와_함께_세션과_쿠키를_정리한다() throws Exception {
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
    void Principal_타입이_비정상이면_INVALID_TOKEN_예외를_던진다() throws Exception {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("notCustomUser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        assertThatThrownBy(() -> resolver.resolveArgument(paramMember(), null, null, null))
                .isInstanceOf(UnauthorizedException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.INVALID_TOKEN);
    }

    @Test
    void Principal_타입이_비정상이어도_allowInvalidToken이면_null을_반환한다() throws Exception {
        TestingAuthenticationToken auth = new TestingAuthenticationToken("notCustomUser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        Object result = resolver.resolveArgument(paramLongAnonymousTrueAllowInvalidToken(), null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void allowInvalidToken은_anonymous_없이_사용할_수_없다() throws Exception {
        setAnonymousAuthentication();

        assertThatThrownBy(() -> resolver.resolveArgument(paramMemberAllowInvalidTokenOnly(), null, null, null))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.PRECONDITION_FAILED);
    }

    @Test
    void 비로그인_요청에_세션_쿠키가_있으면_세션과_쿠키를_정리한다() throws Exception {
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
    void Principal_타입이_비정상이고_세션_쿠키가_있으면_INVALID_TOKEN_예외와_함께_세션과_쿠키를_정리한다() throws Exception {
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

    @Test
    void Principal_타입이_비정상이고_allowInvalidToken이며_세션_쿠키가_있으면_null과_함께_세션과_쿠키를_정리한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie(SESSION_COOKIE_NAME, "session-token"));
        request.getSession();
        TestingAuthenticationToken auth = new TestingAuthenticationToken("notCustomUser", null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);

        NativeWebRequest webRequest = createWebRequest(request, response);

        Object result = resolver.resolveArgument(paramLongAnonymousTrueAllowInvalidToken(), null, webRequest, null);

        assertThat(result).isNull();
        verifyInvalidSessionCookie(response);
        assertThat(request.getSession(false)).isNull();
    }

    @Test
    void OAuth2_사용자의_Member가_null이어도_allowInvalidToken이면_null을_반환한다() throws Exception {
        setLoggedInAuthentication(null);

        Object result = resolver.resolveArgument(paramMemberAnonymousTrueAllowInvalidToken(), null, null, null);

        assertThat(result).isNull();
    }

    @Test
    void OAuth2_사용자의_Member가_null이고_allowInvalidToken이며_세션_쿠키가_있으면_null과_함께_세션과_쿠키를_정리한다() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        request.setCookies(new Cookie(SESSION_COOKIE_NAME, "session-token"));
        request.getSession();
        setLoggedInAuthentication(null);

        NativeWebRequest webRequest = createWebRequest(request, response);

        Object result = resolver.resolveArgument(paramMemberAnonymousTrueAllowInvalidToken(), null, webRequest, null);

        assertThat(result).isNull();
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

    private MethodParameter paramMemberAnonymousTrueAllowInvalidToken() {
        try {
            Method m = StubController.class.getMethod("endpointMemberAnonymousTrueAllowInvalidToken", Member.class);
            return new MethodParameter(m, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private MethodParameter paramMemberAllowInvalidTokenOnly() {
        try {
            Method m = StubController.class.getMethod("endpointMemberAllowInvalidTokenOnly", Member.class);
            return new MethodParameter(m, 0);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }

    private MethodParameter paramLongAnonymousTrueAllowInvalidToken() {
        try {
            Method m = StubController.class.getMethod("endpointLongAnonymousTrueAllowInvalidToken", Long.class);
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
