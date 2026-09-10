package me.bombom.api.v1.auth.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;

import java.net.URI;
import java.time.Instant;
import java.util.Set;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.web.MockHttpServletRequest;

class OAuth2DiagnosticsTest {
    private final MutableClock clock = new MutableClock();
    private final OAuth2Diagnostics diagnostics = new OAuth2Diagnostics(clock, "JSESSIONID_PROD", "instance-1");
    private final Instant receivedAt = Instant.parse("2026-09-10T01:00:00Z");

    @BeforeEach
    void setUp() {
        clock.setInstant(receivedAt);
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.addHeader("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 18_7 like Mac OS X) bombom/1.1.9 Apple ios");
        diagnostics.open(request);
        diagnostics.userInfoToken(new org.springframework.security.oauth2.core.OAuth2AccessToken(
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                "issued-secret-token", receivedAt, receivedAt.plusSeconds(60), Set.of("email", "profile")));
    }

    @AfterEach
    void tearDown() {
        diagnostics.close();
    }

    @Test
    void 실제_전송_토큰의_일치와_유효기간을_원문_없이_기록한다() {
        clock.setInstant(receivedAt.plusSeconds(3));
        outgoing("Bearer issued-secret-token");
        assertThat(diagnostics.snapshot()).containsEntry("token_matches_issued", true)
                .containsEntry("token_age_ms", 3000L).containsEntry("token_remaining_seconds", 57L)
                .containsEntry("client_platform", "ios").containsEntry("app_version", "1.1.9")
                .containsEntry("os_version", "18.7");
        assertThat(diagnostics.snapshot().toString()).doesNotContain("issued-secret-token");
    }

    @Test
    void 다른_토큰과_만료를_구분한다() {
        clock.setInstant(receivedAt.plusSeconds(61));
        outgoing("Bearer another-secret-token");
        assertThat(diagnostics.snapshot()).containsEntry("token_matches_issued", false)
                .containsEntry("token_expired", true).containsEntry("token_remaining_seconds", -1L);
    }

    @Test
    void 헤더_누락과_잘못된_인증_형식을_구분한다() {
        outgoing(null);
        assertThat(diagnostics.snapshot()).containsEntry("authorization_present", false)
                .doesNotContainKey("token_matches_issued");
        outgoing("Basic client-secret");
        assertThat(diagnostics.snapshot()).containsEntry("authorization_present", true)
                .containsEntry("authorization_scheme_valid", false).doesNotContainKey("token_matches_issued");
    }

    @Test
    void 종료된_요청의_토큰이_다음_요청에_남지_않는다() {
        diagnostics.close();
        diagnostics.open(new MockHttpServletRequest("GET", "/login/oauth2/code/google"));
        outgoing("Bearer issued-secret-token");
        assertThat(diagnostics.snapshot()).doesNotContainKey("issued_token_fingerprint")
                .doesNotContainKey("token_matches_issued");
    }

    private void outgoing(String authorization) {
        HttpHeaders headers = new HttpHeaders();
        if (authorization != null) headers.add(HttpHeaders.AUTHORIZATION, authorization);
        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET,
                URI.create("https://www.googleapis.com/oauth2/v3/userinfo"));
        request.getHeaders().putAll(headers);
        diagnostics.outgoing(request);
    }
}
