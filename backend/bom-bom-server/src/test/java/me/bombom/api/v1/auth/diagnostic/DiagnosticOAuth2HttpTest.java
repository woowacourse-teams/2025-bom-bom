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
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthorizationException;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationExchange;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationResponse;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

class DiagnosticOAuth2HttpTest {
    private final OAuth2Diagnostics diagnostics = new OAuth2Diagnostics(Clock.systemUTC(),
            "http-test-diagnostic-secret", "SESSION", "test", "test-release");

    @AfterEach
    void tearDown() {
        diagnostics.close();
    }

    @Test
    void 실제_토큰_교환_후_UserInfo_401의_전송_토큰과_원인을_안전하게_기록한다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer tokenServer = MockRestServiceServer.bindTo(builder).build();
        DiagnosticOAuth2TokenResponseClient tokenClient = new DiagnosticOAuth2TokenResponseClient(builder, diagnostics);
        tokenServer.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withSuccess("{\"access_token\":\"secret-token\",\"token_type\":\"Bearer\",\"expires_in\":3600}", MediaType.APPLICATION_JSON));
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

        var token = tokenClient.getTokenResponse(grant());
        assertThatThrownBy(() -> userService.loadUser(new OAuth2UserRequest(registration(), token.getAccessToken())))
                .isInstanceOf(OAuth2AuthenticationException.class);

        assertThat(diagnostics.snapshot()).containsEntry("token_exchange_status", 200)
                .containsEntry("userinfo_status", 401).containsEntry("token_matches_issued", true)
                .containsEntry("provider_error", "invalid_request").containsEntry("provider_description", "Invalid Credentials")
                .containsEntry("authorization_scheme_valid", true).containsEntry("token_expired", false);
        assertThat(diagnostics.snapshot().toString()).doesNotContain("secret-token", "secret-state", "secret-client");
        tokenServer.verify();
        userServer.verify();
    }

    @Test
    void 토큰_발급_실패를_UserInfo_실패와_구분하고_예외_응답의_비밀값은_버린다() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
        DiagnosticOAuth2TokenResponseClient client = new DiagnosticOAuth2TokenResponseClient(builder, diagnostics);
        server.expect(requestTo("https://oauth2.googleapis.com/token"))
                .andRespond(withStatus(HttpStatus.BAD_REQUEST).contentType(MediaType.APPLICATION_JSON)
                        .body("{\"error\":\"invalid_grant\",\"error_description\":\"secret-code user@example.com\"}"));
        diagnostics.open(new MockHttpServletRequest("GET", "/login/oauth2/code/google"));

        assertThatThrownBy(() -> client.getTokenResponse(grant())).isInstanceOf(OAuth2AuthorizationException.class);

        assertThat(diagnostics.snapshot()).containsEntry("token_exchange_status", 400)
                .containsEntry("token_exchange_success", false).containsEntry("error_code", "invalid_grant")
                .doesNotContainKey("userinfo_status");
        assertThat(diagnostics.snapshot().toString()).doesNotContain("secret-code", "user@example.com");
        server.verify();
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

    private OAuth2AuthorizationCodeGrantRequest grant() {
        var authorization = OAuth2AuthorizationRequest.authorizationCode().authorizationUri("https://accounts.google.com/o/oauth2/v2/auth")
                .clientId("test-client").redirectUri("https://api.bombom.news/login/oauth2/code/google").state("secret-state").build();
        var callback = OAuth2AuthorizationResponse.success("secret-code")
                .redirectUri("https://api.bombom.news/login/oauth2/code/google").state("secret-state").build();
        return new OAuth2AuthorizationCodeGrantRequest(registration(), new OAuth2AuthorizationExchange(authorization, callback));
    }

    private ClientRegistration registration() {
        return ClientRegistration.withRegistrationId("google").clientId("test-client").clientSecret("secret-client")
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .redirectUri("https://api.bombom.news/login/oauth2/code/google").scope("email", "profile")
                .authorizationUri("https://accounts.google.com/o/oauth2/v2/auth").tokenUri("https://oauth2.googleapis.com/token")
                .userInfoUri("https://www.googleapis.com/oauth2/v3/userinfo").userNameAttributeName("sub").build();
    }
}
