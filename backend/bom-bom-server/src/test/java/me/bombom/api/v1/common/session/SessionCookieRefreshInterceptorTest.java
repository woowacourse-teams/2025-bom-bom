package me.bombom.api.v1.common.session;

import static me.bombom.api.v1.common.session.SessionCookieRefreshInterceptor.REFRESHED_AT_ATTRIBUTE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.mock;

import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.domain.Member;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.session.web.http.DefaultCookieSerializer;

class SessionCookieRefreshInterceptorTest {

    private static final String COOKIE_NAME = "JSESSIONID_TEST";
    private static final String COOKIE_DOMAIN = "example.com";
    private static final int COOKIE_MAX_AGE_SECONDS = 10_368_010;
    private static final Instant NOW = Instant.parse("2026-11-21T00:00:00Z");
    private static final Duration REFRESH_INTERVAL = Duration.ofDays(1);

    private SessionCookieRefreshInterceptor interceptor;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    private MockHttpSession session;

    @BeforeEach
    void setUp() {
        DefaultCookieSerializer cookieSerializer = new DefaultCookieSerializer();
        cookieSerializer.setCookieName(COOKIE_NAME);
        cookieSerializer.setDomainName(COOKIE_DOMAIN);
        cookieSerializer.setCookieMaxAge(COOKIE_MAX_AGE_SECONDS);
        cookieSerializer.setUseSecureCookie(true);
        cookieSerializer.setSameSite("None");

        interceptor = new SessionCookieRefreshInterceptor(
                cookieSerializer,
                Clock.fixed(NOW, ZoneId.of("Asia/Seoul")),
                REFRESH_INTERVAL
        );
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        session = new MockHttpSession(null, "session-id-1");
        request.setSession(session);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void 로그인된_요청에서_재발급한_적이_없으면_같은_세션_ID로_쿠키를_다시_내려보낸다() {
        // given
        setLoggedInAuthentication(mock(Member.class));

        // when
        boolean proceed = interceptor.preHandle(request, response, new Object());

        // then
        String setCookie = response.getHeader(HttpHeaders.SET_COOKIE);
        assertSoftly(softly -> {
            softly.assertThat(proceed).isTrue();
            softly.assertThat(setCookie).startsWith(COOKIE_NAME + "=" + encode("session-id-1") + ";");
            softly.assertThat(setCookie).contains("Max-Age=" + COOKIE_MAX_AGE_SECONDS);
            softly.assertThat(setCookie).contains("Domain=" + COOKIE_DOMAIN);
            softly.assertThat(setCookie).contains("Secure", "HttpOnly", "SameSite=None");
            softly.assertThat(session.getAttribute(REFRESHED_AT_ATTRIBUTE)).isEqualTo(NOW.toEpochMilli());
        });
    }

    @Test
    void 재발급_간격이_지나지_않았으면_쿠키를_내려보내지_않는다() {
        // given
        setLoggedInAuthentication(mock(Member.class));
        long refreshedAt = NOW.minus(Duration.ofHours(23)).toEpochMilli();
        session.setAttribute(REFRESHED_AT_ATTRIBUTE, refreshedAt);

        // when
        interceptor.preHandle(request, response, new Object());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
            softly.assertThat(session.getAttribute(REFRESHED_AT_ATTRIBUTE)).isEqualTo(refreshedAt);
        });
    }

    @Test
    void 재발급_간격이_지났으면_쿠키를_다시_내려보내고_시각을_갱신한다() {
        // given
        setLoggedInAuthentication(mock(Member.class));
        session.setAttribute(REFRESHED_AT_ATTRIBUTE, NOW.minus(REFRESH_INTERVAL).toEpochMilli());

        // when
        interceptor.preHandle(request, response, new Object());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).startsWith(COOKIE_NAME + "=");
            softly.assertThat(session.getAttribute(REFRESHED_AT_ATTRIBUTE)).isEqualTo(NOW.toEpochMilli());
        });
    }

    @Test
    void 익명_요청이면_쿠키를_내려보내지_않는다() {
        // given
        SecurityContextHolder.getContext().setAuthentication(new AnonymousAuthenticationToken(
                "key",
                "anonymousUser",
                List.of(new SimpleGrantedAuthority("ROLE_ANONYMOUS"))
        ));

        // when
        interceptor.preHandle(request, response, new Object());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
            softly.assertThat(session.getAttribute(REFRESHED_AT_ATTRIBUTE)).isNull();
        });
    }

    @Test
    void 회원_정보가_없는_인증이면_쿠키를_내려보내지_않는다() {
        // given
        setLoggedInAuthentication(null);

        // when
        interceptor.preHandle(request, response, new Object());

        // then
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    @Test
    void 세션이_없으면_세션을_만들지_않고_쿠키도_내려보내지_않는다() {
        // given
        setLoggedInAuthentication(mock(Member.class));
        MockHttpServletRequest requestWithoutSession = new MockHttpServletRequest();

        // when
        interceptor.preHandle(requestWithoutSession, response, new Object());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
            softly.assertThat(requestWithoutSession.getSession(false)).isNull();
        });
    }

    private void setLoggedInAuthentication(Member member) {
        CustomOAuth2User user = new CustomOAuth2User(Map.of("name", "tester"), member, null, null);
        OAuth2AuthenticationToken authentication = new OAuth2AuthenticationToken(
                user,
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                "registrationId"
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private String encode(String sessionId) {
        return Base64.getEncoder().encodeToString(sessionId.getBytes(StandardCharsets.UTF_8));
    }
}
