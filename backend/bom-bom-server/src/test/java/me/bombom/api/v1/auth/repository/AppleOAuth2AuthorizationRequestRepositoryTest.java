package me.bombom.api.v1.auth.repository;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.Cookie;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;
import me.bombom.api.v1.auth.dto.PendingAppleAuthorizationRequest;
import me.bombom.api.v1.auth.service.AppleAuthorizationRequestService;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

class AppleOAuth2AuthorizationRequestRepositoryTest {

    private static final String COOKIE = "__Host-BOMBOM_APPLE_AUTH_DEV";
    private static final Instant NOW = Instant.parse("2026-10-21T00:00:00Z");
    private final MemoryStore store = new MemoryStore();
    private final AppleOAuth2AuthorizationRequestRepository repository = repositoryAt(NOW);

    @Test
    void 애플_요청은_로그인_세션과_분리하고_짧은_수명의_보안_쿠키를_발급한다() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        repository.saveAuthorizationRequest(authorization("apple"), request, response);

        assertThat(request.getSession(false)).isNull();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE))
                .contains(COOKIE + "=", "Path=/;", "Max-Age=300", "Secure", "HttpOnly", "SameSite=None")
                .doesNotContain("Domain=", "JSESSIONID", "test-state");
        assertThat(store.requests).hasSize(1);
        assertThat(store.requests.keySet().iterator().next()).hasSize(64);
    }

    @Test
    void 기존_세션_쿠키_없이_콜백을_검증하고_필요한_문맥만_복원한다() {
        MockHttpServletRequest start = new MockHttpServletRequest();
        start.getSession().setAttribute("redirectUrl", "https://dev.bombom.news/blog");
        start.getSession().setAttribute("pendingWithdraw", true);
        start.getSession().setAttribute("withdrawMemberId", 1L);
        start.getSession().setAttribute("SPRING_SECURITY_CONTEXT", "must-not-copy");
        MockHttpServletRequest callback = startAndCallback(start);
        callback.setParameter("user", "{\"name\":{\"firstName\":\"test\"}}");
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertThat(callback.getSession(false)).isNull();
        assertThat(repository.removeAuthorizationRequest(callback, response)).isNotNull();
        assertThat(callback.getSession().getAttribute("redirectUrl")).isEqualTo("https://dev.bombom.news/blog");
        assertThat(callback.getSession().getAttribute("withdrawMemberId")).isEqualTo(1L);
        assertThat(callback.getSession().getAttribute("SPRING_SECURITY_CONTEXT")).isNull();
        assertThat(callback.getSession().getAttribute("appleUserParam")).isNotNull();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).contains("Max-Age=0");
        assertThat(repository.removeAuthorizationRequest(callback, new MockHttpServletResponse())).isNull();
    }

    @Test
    void 애플_취소_응답은_요청만_소비하고_새_일반_세션을_만들지_않는다() {
        var callback = startAndCallback(new MockHttpServletRequest());
        callback.setParameter("error", "access_denied");
        var response = new MockHttpServletResponse();
        assertThat(repository.removeAuthorizationRequest(callback, response)).isNotNull();
        assertThat(callback.getSession(false)).isNull();
        assertThat(response.getHeader("Set-Cookie")).contains("Max-Age=0");
        assertThat(repository.removeAuthorizationRequest(callback, new MockHttpServletResponse())).isNull();
    }

    @Test
    void 임시_쿠키가_없는_콜백은_거부한다() {
        MockHttpServletRequest callback = startAndCallback(new MockHttpServletRequest());
        callback.setCookies();
        assertThat(repository.removeAuthorizationRequest(callback, new MockHttpServletResponse())).isNull();
        assertThat(callback.getSession(false)).isNull();
    }

    @Test
    void 다른_브라우저의_쿠키나_변조된_state는_거부한다() {
        MockHttpServletRequest first = startAndCallback(new MockHttpServletRequest());
        MockHttpServletRequest second = startAndCallback(new MockHttpServletRequest(), "another-state");
        first.setParameter("state", "wrong-state");
        assertThat(repository.removeAuthorizationRequest(first, new MockHttpServletResponse())).isNull();
        first.setParameter("state", "test-state");
        first.setCookies(second.getCookies());
        assertThat(repository.removeAuthorizationRequest(first, new MockHttpServletResponse())).isNull();
        assertThat(repository.removeAuthorizationRequest(second, new MockHttpServletResponse())).isNotNull();
    }

    @Test
    void 만료된_요청과_중복_소비를_거부한다() {
        MockHttpServletRequest callback = startAndCallback(new MockHttpServletRequest());
        assertThat(repositoryAt(NOW.plusSeconds(300)).removeAuthorizationRequest(callback, new MockHttpServletResponse())).isNull();
        assertThat(callback.getSession(false)).isNull();
    }

    @Test
    void 이전_탭의_콜백이_최신_로그인_쿠키를_지우지_않는다() {
        var previous = startAndCallback(new MockHttpServletRequest(), "previous-state");
        var latest = startAndCallback(new MockHttpServletRequest(), "latest-state");
        previous.setCookies(latest.getCookies());
        var response = new MockHttpServletResponse();

        assertThat(repository.removeAuthorizationRequest(previous, response)).isNull();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
        assertThat(repository.removeAuthorizationRequest(latest, new MockHttpServletResponse())).isNotNull();
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings = {"", "short", "A*43", "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"})
    void 형식이_잘못된_임시_쿠키는_인증_세션을_만들지_않는다(String value) {
        var callback = startAndCallback(new MockHttpServletRequest());
        callback.setCookies(new Cookie(COOKIE, value));
        assertThat(repository.removeAuthorizationRequest(callback, new MockHttpServletResponse())).isNull();
        assertThat(callback.getSession(false)).isNull();
    }

    @Test
    void 구글_로그인은_기존_세션_기반_동작을_유지한다() {
        MockHttpServletRequest start = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        repository.saveAuthorizationRequest(authorization("google"), start, response);
        MockHttpServletRequest callback = new MockHttpServletRequest();
        callback.setServletPath("/login/oauth2/code/google");
        callback.setSession((org.springframework.mock.web.MockHttpSession) start.getSession());
        callback.setParameter("state", "test-state");
        assertThat(repository.removeAuthorizationRequest(callback, response).<String>getAttribute("registration_id")).isEqualTo("google");
        assertThat(store.requests).isEmpty();
        assertThat(response.getHeader(HttpHeaders.SET_COOKIE)).isNull();
    }

    private MockHttpServletRequest startAndCallback(MockHttpServletRequest start) {
        return startAndCallback(start, "test-state");
    }

    private MockHttpServletRequest startAndCallback(MockHttpServletRequest start, String state) {
        MockHttpServletResponse response = new MockHttpServletResponse();
        var authorization = OAuth2AuthorizationRequest.from(authorization("apple")).state(state).build();
        repository.saveAuthorizationRequest(authorization, start, response);
        String token = response.getHeader(HttpHeaders.SET_COOKIE).split(";", 2)[0].split("=", 2)[1];
        MockHttpServletRequest callback = new MockHttpServletRequest("POST", "/login/sso/verify/success/apple");
        callback.setServletPath("/login/sso/verify/success/apple");
        callback.setCookies(new Cookie(COOKIE, token));
        callback.setParameter("state", state);
        return callback;
    }

    private AppleOAuth2AuthorizationRequestRepository repositoryAt(Instant instant) {
        return new AppleOAuth2AuthorizationRequestRepository(store, Clock.fixed(instant, ZoneOffset.UTC), COOKIE);
    }

    private OAuth2AuthorizationRequest authorization(String provider) {
        return OAuth2AuthorizationRequest.authorizationCode().authorizationUri("https://provider.example/authorize")
                .clientId("test-client").redirectUri("https://api-dev.bombom.news/login/oauth2/code/" + provider)
                .state("test-state").attributes(Map.of("registration_id", provider)).build();
    }

    private static class MemoryStore extends AppleAuthorizationRequestService {

        MemoryStore() {
            super(null, Clock.fixed(NOW, ZoneOffset.UTC));
        }
        private final Map<String, PendingAppleAuthorizationRequest> requests = new HashMap<>();
        private final Map<String, Instant> expirations = new HashMap<>();

        public void save(String key, PendingAppleAuthorizationRequest pending, Instant expiresAt) {
            requests.put(key, pending);
            expirations.put(key, expiresAt);
        }

        public PendingAppleAuthorizationRequest find(String key, String state, Instant now) {
            PendingAppleAuthorizationRequest pending = requests.get(key);
            return pending != null && pending.authorizationRequest().getState().equals(state)
                    && expirations.get(key).isAfter(now) ? pending : null;
        }

        public boolean consume(String key, Instant now) {
            return expirations.containsKey(key) && expirations.get(key).isAfter(now) && requests.remove(key) != null;
        }
    }
}
