package me.bombom.api.v1.auth.diagnostic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import java.time.Clock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class DiagnosticOAuth2HttpTest {
    private final OAuth2Diagnostics diagnostics = new OAuth2Diagnostics(Clock.systemUTC(), "SESSION", "test");

    @AfterEach
    void tearDown() {
        diagnostics.close();
    }

    @Test
    void UserInfo_401의_전송_토큰과_응답을_안전하게_기록한다() {
        RestTemplate template = new RestTemplate();
        MockRestServiceServer userServer = MockRestServiceServer.bindTo(template).build();
        DiagnosticOAuth2UserService userService = new DiagnosticOAuth2UserService(template, diagnostics);
        userServer.expect(requestTo("https://www.googleapis.com/oauth2/v3/userinfo"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer secret-token"))
                .andRespond(withStatus(HttpStatus.UNAUTHORIZED).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_request\",\"error_description\":\"Invalid Credentials\"}"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/login/oauth2/code/google");
        request.setParameter("state", "secret-state");
        diagnostics.open(request);

        var now = java.time.Instant.now();
        var token = new org.springframework.security.oauth2.core.OAuth2AccessToken(
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                "secret-token", now, now.plusSeconds(3600));
        assertThatThrownBy(() -> userService.loadUser(new OAuth2UserRequest(registration(), token)))
                .isInstanceOf(OAuth2AuthenticationException.class)
                .satisfies(diagnostics::failureDetails);

        assertThat(diagnostics.snapshot()).containsEntry("userinfo_status", 401).containsEntry("token_matches_issued", true)
                .containsEntry("provider_error", "invalid_request").containsEntry("provider_description", "Invalid Credentials")
                .containsEntry("authorization_scheme_valid", true).containsEntry("token_expired", false);
        assertThat(diagnostics.snapshot().toString()).doesNotContain("secret-token", "secret-state", "secret-client");
        userServer.verify();
    }

    @Test
    void 정상_UserInfo_응답은_그대로_반환하고_프로필은_기록하지_않는다() {
        RestTemplate template = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(template).build();
        DiagnosticOAuth2UserService service = new DiagnosticOAuth2UserService(template, diagnostics);
        server.expect(requestTo("https://www.googleapis.com/oauth2/v3/userinfo"))
                .andRespond(withSuccess("{\"sub\":\"user-123\",\"email\":\"private@example.com\"}", MediaType.APPLICATION_JSON));
        diagnostics.open(new MockHttpServletRequest("GET", "/login/oauth2/code/google"));
        var token = new org.springframework.security.oauth2.core.OAuth2AccessToken(
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER, "secret-token", null, null);
        var user = service.loadUser(new OAuth2UserRequest(registration(), token));
        assertThat(user.getName()).isEqualTo("user-123");
        assertThat(diagnostics.snapshot()).containsEntry("userinfo_status", 200);
        assertThat(diagnostics.snapshot().toString()).doesNotContain("private@example.com", "user-123");
        server.verify();
    }

    @Test
    void 진단용_응답_상태_조회가_실패해도_실제_응답은_유지한다() {
        RestTemplate template = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(template).build();
        DiagnosticOAuth2UserService service = new DiagnosticOAuth2UserService(template, diagnostics);
        server.expect(requestTo("https://www.googleapis.com/oauth2/v3/userinfo")).andRespond(request -> {
            var response = new org.springframework.mock.http.client.MockClientHttpResponse(
                    "{\"sub\":\"user-123\"}".getBytes(java.nio.charset.StandardCharsets.UTF_8), HttpStatus.OK) {
                private int reads;
                @Override
                public org.springframework.http.HttpStatusCode getStatusCode() {
                    if (reads++ == 0) throw new IllegalStateException("diagnostic-only read failure");
                    return HttpStatus.OK;
                }
            };
            response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
            return response;
        });
        diagnostics.open(new MockHttpServletRequest("GET", "/login/oauth2/code/google"));
        var token = new org.springframework.security.oauth2.core.OAuth2AccessToken(
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER, "secret-token", null, null);
        assertThat(service.loadUser(new OAuth2UserRequest(registration(), token)).getName()).isEqualTo("user-123");
        assertThat(diagnostics.snapshot()).containsEntry("diagnostic_error", true);
        server.verify();
    }

    @Test
    void 기본_토큰_교환을_계측하지_않아도_UserInfo_전송_토큰을_비교한다() {
        RestTemplate template = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(template).build();
        DiagnosticOAuth2UserService service = new DiagnosticOAuth2UserService(template, diagnostics);
        server.expect(requestTo("https://www.googleapis.com/oauth2/v3/userinfo"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer secret-token"))
                .andRespond(withSuccess("{\"sub\":\"user-123\"}", MediaType.APPLICATION_JSON));
        diagnostics.open(new MockHttpServletRequest("GET", "/login/oauth2/code/google"));
        var now = java.time.Instant.now();
        var token = new org.springframework.security.oauth2.core.OAuth2AccessToken(
                org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                "secret-token", now, now.plusSeconds(60));

        assertThat(service.loadUser(new OAuth2UserRequest(registration(), token)).getName()).isEqualTo("user-123");
        assertThat(diagnostics.snapshot()).containsEntry("token_matches_issued", true)
                .containsEntry("token_expired", false);
        server.verify();
    }

    @Test
    void 진단_시계가_실패해도_UserInfo_요청과_응답을_유지한다() {
        Clock brokenClock = new Clock() {
            public java.time.ZoneId getZone() { return java.time.ZoneOffset.UTC; }
            public Clock withZone(java.time.ZoneId zone) { return this; }
            public java.time.Instant instant() { throw new IllegalStateException("clock failed"); }
        };
        OAuth2Diagnostics broken = new OAuth2Diagnostics(brokenClock, "SESSION", "test");
        RestTemplate template = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(template).build();
        DiagnosticOAuth2UserService service = new DiagnosticOAuth2UserService(template, broken);
        server.expect(requestTo("https://www.googleapis.com/oauth2/v3/userinfo"))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer secret-token"))
                .andRespond(withSuccess("{\"sub\":\"user-123\"}", MediaType.APPLICATION_JSON));
        broken.open(new MockHttpServletRequest("GET", "/login/oauth2/code/google"));
        try {
            var token = new org.springframework.security.oauth2.core.OAuth2AccessToken(
                    org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER, "secret-token", null, null);
            assertThat(service.loadUser(new OAuth2UserRequest(registration(), token)).getName()).isEqualTo("user-123");
            assertThat(broken.snapshot()).containsEntry("diagnostic_error", true);
            server.verify();
        } finally {
            broken.close();
        }
    }

    private ClientRegistration registration() {
        return ClientRegistration.withRegistrationId("google").clientId("test-client").clientSecret("secret-client")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.bombom.news/login/oauth2/code/google").scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth").tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo").userNameAttributeName("sub").build();
    }
}
