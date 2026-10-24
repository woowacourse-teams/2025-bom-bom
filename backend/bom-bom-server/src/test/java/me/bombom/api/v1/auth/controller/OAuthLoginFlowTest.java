package me.bombom.api.v1.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.sun.net.httpserver.HttpServer;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import me.bombom.api.v1.auth.handler.OAuth2LoginSuccessHandler;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.acceptance.ResetsAcceptanceData;
import me.bombom.support.auth.FakeAppleOAuth2Service;
import me.bombom.support.auth.FakeOAuth2LoginSuccessHandler;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

@AcceptanceTest({"acceptance/common/member.json", "acceptance/member/member.json", "acceptance/auth/google-oauth-flow.json"})
@TestPropertySource(properties = {
        "app.security.csrf-enabled=true",
        "server.servlet.session.cookie.name=JSESSIONID_OAUTH_TEST",
        "server.servlet.session.cookie.domain=",
        "server.servlet.session.cookie.same-site=Lax",
        "app.auth.apple-request-cookie-name=__Host-BOMBOM_APPLE_AUTH_FLOW"
})
class OAuthLoginFlowTest {

    private static final String COOKIE = "__Host-BOMBOM_APPLE_AUTH_FLOW";
    private static final RSAKey KEY = createKey();
    private static final Map<String, String> NONCES = new ConcurrentHashMap<>();
    private static final AtomicInteger TOKEN_REQUESTS = new AtomicInteger();
    private static final AtomicInteger TOKEN_FAILURES = new AtomicInteger();
    private static final HttpServer IDP = startIdp();
    private final List<String> states = new ArrayList<>();

    @Autowired private FakeAppleOAuth2Service appleService;
    @Autowired private MemberRepository memberRepository;
    @Autowired private JdbcTemplate jdbcTemplate;
    @Autowired @Qualifier("appleOAuth2Service")
    private AppleOAuth2Service realAppleService;
    @Autowired @Qualifier("OAuth2LoginSuccessHandler")
    private OAuth2LoginSuccessHandler realSuccessHandler;
    @Autowired private FakeOAuth2LoginSuccessHandler successHandler;

    @DynamicPropertySource
    static void provider(DynamicPropertyRegistry properties) {
        String base = "http://127.0.0.1:" + IDP.getAddress().getPort();
        properties.add("spring.security.oauth2.client.provider.apple.token-uri", () -> base + "/token");
        properties.add("spring.security.oauth2.client.provider.apple.jwk-set-uri", () -> base + "/jwks");
        properties.add("spring.security.oauth2.client.provider.apple.user-info-uri", () -> "");
        properties.add("spring.security.oauth2.client.registration.apple.client-id", () -> "test-apple-client");
        properties.add("spring.security.oauth2.client.registration.apple.scope", () -> "openid,name,email");
        properties.add("spring.security.oauth2.client.registration.apple.redirect-uri", () -> "{baseUrl}/login/sso/verify/success/apple");
        properties.add("spring.security.oauth2.client.provider.google.token-uri", () -> base + "/token");
        properties.add("spring.security.oauth2.client.provider.google.user-info-uri", () -> base + "/userinfo");
        properties.add("spring.security.oauth2.client.registration.google.client-id", () -> "test-google-client");
        properties.add("spring.security.oauth2.client.registration.google.client-secret", () -> "test-google-secret");
        properties.add("spring.security.oauth2.client.registration.google.scope", () -> "email,profile");
    }

    @BeforeEach
    void setUp() {
        NONCES.clear();
        TOKEN_REQUESTS.set(0);
        TOKEN_FAILURES.set(0);
        appleService.allowWebLogin(memberRepository.findById(1L).orElseThrow());
    }

    @AfterEach
    void cleanUp() {
        for (String state : states) jdbcTemplate.update("DELETE FROM apple_authorization_request WHERE state = ?", state);
        assertThat(TOKEN_FAILURES.get()).as("Local IdP must return valid test responses").isZero();
    }

    @AfterAll
    static void stopIdp() {
        IDP.stop(0);
    }

    @Test
    void 기존_세션_없이_서명된_ID토큰으로_로그인하고_새_세션과_CSRF로_API를_사용한다() {
        Response before = RestAssured.given().get("/api/v1/auth/csrf").then().statusCode(200).extract().response();
        Started started = start(before.getCookies());
        NONCES.put("valid-code", started.nonce());
        Response login = callback(started, "valid-code", started.cookie(), started.state()).then().statusCode(200).extract().response();
        assertThat(login.getCookie("JSESSIONID_OAUTH_TEST")).isNotBlank().isNotEqualTo(before.getCookie("JSESSIONID_OAUTH_TEST"));
        Response member = RestAssured.given().cookies(login.getCookies()).get("/api/v1/members/me")
                .then().statusCode(200).extract().response();
        assertThat(member.jsonPath().getString("email")).isEqualTo("acceptance@bombom.news");
        change(login.getCookies(), before.jsonPath().getString("token")).then().statusCode(403);
        Response csrf = RestAssured.given().cookies(login.getCookies()).get("/api/v1/auth/csrf")
                .then().statusCode(200).extract().response();
        change(login.getCookies(), csrf.jsonPath().getString("token")).then().statusCode(400);
        callback(started, "valid-code", started.cookie(), started.state()).then().statusCode(302);
        assertThat(TOKEN_REQUESTS.get()).isEqualTo(1);
    }

    @Test
    void 유효한_서명이어도_nonce가_다르면_로그인하지_못한다() {
        Started started = start(Map.of());
        NONCES.put("wrong-nonce-code", "wrong-nonce");
        Response response = callback(started, "wrong-nonce-code", started.cookie(), started.state())
                .then().statusCode(302).extract().response();
        RestAssured.given().cookies(response.getCookies()).get("/api/v1/members/me").then().statusCode(401);
        assertThat(TOKEN_REQUESTS.get()).isEqualTo(1);
    }

    @Test
    void 쿠키나_state가_틀리면_토큰_교환_이전에_거부한다() {
        Started started = start(Map.of());
        callback(started, "unused-code", "A".repeat(43), started.state()).then().statusCode(302);
        callback(started, "unused-code", started.cookie(), "wrong-state").then().statusCode(302);
        assertThat(TOKEN_REQUESTS.get()).isZero();
    }

    @Test
    void 구글은_기존_세션으로_GET_콜백을_처리하고_실제_회원_조회와_CSRF를_유지한다() {
        Response before = RestAssured.given().get("/api/v1/auth/csrf").then().statusCode(200).extract().response();
        Response started = RestAssured.given().redirects().follow(false).cookies(before.getCookies())
                .get("/oauth2/authorization/google").then().statusCode(302).extract().response();
        String state = UriComponentsBuilder.fromUriString(started.header("Location"))
                .build().getQueryParams().getFirst("state");
        state = URLDecoder.decode(state, StandardCharsets.UTF_8);
        Response login = RestAssured.given().redirects().follow(false).cookies(before.getCookies())
                .queryParam("state", state).queryParam("code", "google-code")
                .get("/login/oauth2/code/google").then().statusCode(200).extract().response();
        assertThat(login.getCookie("JSESSIONID_OAUTH_TEST")).isNotBlank().isNotEqualTo(before.getCookie("JSESSIONID_OAUTH_TEST"));
        Response member = RestAssured.given().cookies(login.getCookies()).get("/api/v1/members/me")
                .then().statusCode(200).extract().response();
        assertThat(member.jsonPath().getString("email")).isEqualTo("google-flow@example.test");
        change(login.getCookies(), before.jsonPath().getString("token")).then().statusCode(403);
        assertThat(TOKEN_REQUESTS.get()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"missing-exp-code", "missing-aud-code", "missing-sub-code", "future-iat-code", "missing-azp-code", "missing-iat-code", "missing-nonce-code", "wrong-azp-code"})
    void OIDC_필수_클레임이_잘못되면_서명이_맞아도_로그인에_사용할_수_없다(String code) {
        Started started = start(Map.of());
        NONCES.put(code, started.nonce());
        Response response = callback(started, code, started.cookie(), started.state())
                .then().statusCode(302).extract().response();
        RestAssured.given().cookies(response.getCookies()).get("/api/v1/members/me").then().statusCode(401);
        assertThat(TOKEN_REQUESTS.get()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {"expired-code", "wrong-issuer-code", "wrong-audience-code", "invalid-signature-code",
            "token-error-code", "malformed-response-code", "missing-id-token-code"})
    void 토큰_교환과_검증이_실패하면_인증하지_않고_요청을_다시_사용할_수_없다(String code) {
        Started started = start(Map.of());
        NONCES.put(code, started.nonce());
        Response response = callback(started, code, started.cookie(), started.state())
                .then().statusCode(302).extract().response();
        RestAssured.given().cookies(response.getCookies()).get("/api/v1/members/me").then().statusCode(401);
        assertThat(response.getHeaders().getValues("Set-Cookie").stream()
                .anyMatch(value -> value.startsWith(COOKIE + "=") && value.contains("Max-Age=0"))).isTrue();
        callback(started, code, started.cookie(), started.state()).then().statusCode(302);
        assertThat(TOKEN_REQUESTS.get()).isEqualTo(1);
    }

    @Test
    void 표준_애플_POST_콜백에서도_동일한_인증_검증을_적용한다() {
        Started started = start(Map.of());
        NONCES.put("standard-code", started.nonce());
        Response login = RestAssured.given().redirects().follow(false).cookie(COOKIE, started.cookie())
                .header("Origin", "https://appleid.apple.com").contentType(ContentType.URLENC)
                .formParam("code", "standard-code").formParam("state", started.state())
                .post("/login/oauth2/code/apple").then().statusCode(200).extract().response();
        RestAssured.given().cookies(login.getCookies()).get("/api/v1/members/me").then().statusCode(200);
        assertThat(TOKEN_REQUESTS.get()).isEqualTo(1);
    }

    @Test
    void 애플_요청을_구글_콜백으로_바꿔_로그인할_수_없다() {
        Started started = start(Map.of());
        RestAssured.given().redirects().follow(false).cookie(COOKIE, started.cookie())
                .queryParam("code", "unused-code").queryParam("state", started.state())
                .get("/login/oauth2/code/google").then().statusCode(302);
        assertThat(TOKEN_REQUESTS.get()).isZero();
        NONCES.put("right-provider-code", started.nonce());
        callback(started, "right-provider-code", started.cookie(), started.state()).then().statusCode(200);
    }

    @Test
    void 이전_탭의_콜백을_거부해도_최신_탭의_로그인은_계속할_수_있다() {
        Started previous = start(Map.of());
        Started latest = start(Map.of());
        Response rejected = callback(previous, "old-code", latest.cookie(), previous.state())
                .then().statusCode(302).extract().response();
        assertThat(rejected.getHeaders().getValues("Set-Cookie").stream()
                .anyMatch(value -> value.startsWith(COOKIE + "="))).isFalse();
        assertThat(TOKEN_REQUESTS.get()).isZero();
        NONCES.put("latest-code", latest.nonce());
        Response login = callback(latest, "latest-code", latest.cookie(), latest.state())
                .then().statusCode(200).extract().response();
        RestAssured.given().cookies(login.getCookies()).get("/api/v1/members/me").then().statusCode(200);
        assertThat(TOKEN_REQUESTS.get()).isEqualTo(1);
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "\t", "\u00a0"})
    void state_뒤의_공백도_변조로_거부하고_원래_요청은_보존한다(String suffix) {
        Started started = start(Map.of());
        Response rejected = callback(started, "unused-code", started.cookie(), started.state() + suffix)
                .then().statusCode(302).extract().response();
        assertThat(TOKEN_REQUESTS.get()).isZero();
        assertThat(rejected.getHeaders().getValues("Set-Cookie").stream()
                .anyMatch(value -> value.startsWith(COOKIE + "="))).isFalse();
        NONCES.put("original-code", started.nonce());
        callback(started, "original-code", started.cookie(), started.state()).then().statusCode(200);
    }

    @Test
    @ResetsAcceptanceData
    void 최초_애플_user를_실제_서비스가_읽어_가입하고_재로그인시_user가_없어도_회원을_유지한다() throws Exception {
        useRealAppleLogin();
        String userJson = new ObjectMapper().writeValueAsString(Map.of(
                "email", "apple@bombom.news", "name", Map.of("firstName", "동준", "lastName", "최")));
        Response login = realAppleCallback("first-consent-code", userJson);
        var query = signupQuery(login);
        assertThat(query.getFirst("email")).isEqualTo("apple");
        assertThat(URLDecoder.decode(query.getFirst("name"), StandardCharsets.UTF_8)).isEqualTo("최동준");

        Response csrf = RestAssured.given().cookies(login.getCookies()).get("/api/v1/auth/csrf")
                .then().statusCode(200).extract().response();
        Response signup = RestAssured.given().cookies(login.getCookies())
                .header("X-CSRF-TOKEN", csrf.jsonPath().getString("token"))
                .contentType(ContentType.JSON).body(Map.of("email", "apple@bombom.news", "nickname", "최동준"))
                .post("/api/v1/auth/signup").then().statusCode(201).extract().response();
        Response member = RestAssured.given().cookies(signup.getCookies()).get("/api/v1/members/me")
                .then().statusCode(200).extract().response();
        assertThat(member.jsonPath().getString("email")).isEqualTo("apple@bombom.news");
        assertThat(member.jsonPath().getString("nickname")).isEqualTo("최동준");

        Response again = realAppleCallback("returning-code", null);
        assertThat(URI.create(again.header("Location")).getPath()).isEqualTo("/");
        Response returning = RestAssured.given().cookies(again.getCookies()).get("/api/v1/members/me")
                .then().statusCode(200).extract().response();
        assertThat(returning.jsonPath().getInt("id")).isEqualTo(member.jsonPath().getInt("id"));
        assertThat(returning.jsonPath().getString("email")).isEqualTo("apple@bombom.news");
        assertThat(returning.jsonPath().getString("nickname")).isEqualTo("최동준");
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(strings = {"{broken-json"})
    void user가_없거나_깨져도_실제_서비스는_ID토큰_이메일로_가입_화면을_구성한다(String userJson) {
        useRealAppleLogin();
        var query = signupQuery(realAppleCallback("fallback-code", userJson));
        assertThat(query.getFirst("email")).isEqualTo("token");
        assertThat(query.getFirst("name")).isNotBlank();
    }

    private void useRealAppleLogin() {
        appleService.delegateWebLoginTo(realAppleService);
        successHandler.delegateTo(realSuccessHandler);
    }

    private Response realAppleCallback(String code, String userJson) {
        Started started = start(Map.of());
        NONCES.put(code, started.nonce());
        var request = RestAssured.given().redirects().follow(false).cookie(COOKIE, started.cookie())
                .header("Origin", "https://appleid.apple.com").contentType("application/x-www-form-urlencoded; charset=UTF-8")
                .formParam("code", code).formParam("state", started.state());
        if (userJson != null) request.formParam("user", userJson);
        return request.post("/login/sso/verify/success/apple").then().statusCode(302).extract().response();
    }

    private MultiValueMap<String, String> signupQuery(Response response) {
        assertThat(URI.create(response.header("Location")).getPath()).isEqualTo("/signup");
        return UriComponentsBuilder.fromUriString(response.header("Location"))
                .build().getQueryParams();
    }

    private Response change(Map<String, String> cookies, String token) {
        return RestAssured.given().cookies(cookies).header("X-CSRF-TOKEN", token)
                .contentType(ContentType.JSON).body(Map.of("nickname", "invalid..nickname"))
                .patch("/api/v1/members/me");
    }

    private Started start(Map<String, String> cookies) {
        Response response = RestAssured.given().redirects().follow(false).cookies(cookies)
                .get("/oauth2/authorization/apple").then().statusCode(302).extract().response();
        var query = UriComponentsBuilder.fromUriString(response.header("Location")).build().getQueryParams();
        String state = URLDecoder.decode(query.getFirst("state"), StandardCharsets.UTF_8);
        String nonce = URLDecoder.decode(query.getFirst("nonce"), StandardCharsets.UTF_8);
        states.add(state);
        return new Started(state, nonce, response.getCookie(COOKIE));
    }

    private Response callback(Started started, String code, String cookie, String state) {
        return RestAssured.given().redirects().follow(false).cookie(COOKIE, cookie)
                .header("Origin", "https://appleid.apple.com").contentType(ContentType.URLENC)
                .formParam("code", code).formParam("state", state)
                .post("/login/sso/verify/success/apple");
    }

    private record Started(String state, String nonce, String cookie) { }

    private static RSAKey createKey() {
        try { return new RSAKeyGenerator(2048).keyID("flow-test-key").generate(); }
        catch (Exception exception) { throw new IllegalStateException(exception); }
    }

    private static HttpServer startIdp() {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/jwks", exchange -> {
                byte[] body = new JWKSet(KEY.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.createContext("/userinfo", exchange -> {
                if (!"Bearer test-access-token".equals(exchange.getRequestHeaders().getFirst("Authorization"))) {
                    TOKEN_FAILURES.incrementAndGet();
                    exchange.sendResponseHeaders(401, -1);
                    exchange.close();
                    return;
                }
                byte[] body = new ObjectMapper().writeValueAsBytes(Map.of("sub", "test-google-subject",
                        "email", "google-flow@example.test", "name", "Google flow"));
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.createContext("/token", exchange -> {
                TOKEN_REQUESTS.incrementAndGet();
                try {
                    Map<String, String> form = new HashMap<>();
                    for (String pair : new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8).split("&")) {
                        String[] parts = pair.split("=", 2);
                        form.put(URLDecoder.decode(parts[0], StandardCharsets.UTF_8), URLDecoder.decode(parts[1], StandardCharsets.UTF_8));
                    }
                    if (!"authorization_code".equals(form.get("grant_type")) || form.get("redirect_uri") == null) {
                        throw new IllegalArgumentException("Invalid test token exchange");
                    }
                    String code = form.get("code");
                    if ("token-error-code".equals(code) || "malformed-response-code".equals(code)) {
                        byte[] body = (code.equals("token-error-code") ? "{\"error\":\"invalid_grant\"}" : "not-json").getBytes(StandardCharsets.UTF_8);
                        exchange.getResponseHeaders().set("Content-Type", "application/json");
                        exchange.sendResponseHeaders(code.equals("token-error-code") ? 400 : 200, body.length);
                        exchange.getResponseBody().write(body);
                        return;
                    }
                    var claims = new JWTClaimsSet.Builder().issuer("https://appleid.apple.com").audience("test-apple-client")
                            .subject("test-apple-subject").issueTime(Date.from(Instant.now().minusSeconds(5)))
                            .expirationTime(Date.from(Instant.now().plusSeconds(600)))
                            .claim("nonce", NONCES.get(form.get("code"))).claim("email", "token@bombom.news").build();
                    if ("missing-exp-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).expirationTime(null).build();
                    if ("missing-aud-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).claim("aud", null).build();
                    if ("missing-sub-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).subject(null).build();
                    if ("future-iat-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).issueTime(Date.from(Instant.now().plusSeconds(300))).build();
                    if ("missing-azp-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).audience(List.of("test-apple-client", "another-client")).build();
                    if ("missing-iat-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).issueTime(null).build();
                    if ("missing-nonce-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).claim("nonce", null).build();
                    if ("wrong-azp-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).audience(List.of("test-apple-client", "another-client")).claim("azp", "another-client").build();
                    if ("expired-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).issueTime(Date.from(Instant.now().minusSeconds(600))).expirationTime(Date.from(Instant.now().minusSeconds(300))).build();
                    if ("wrong-issuer-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).issuer("https://evil.example").build();
                    if ("wrong-audience-code".equals(code)) claims = new JWTClaimsSet.Builder(claims).audience("another-client").build();
                    var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(KEY.getKeyID()).build(), claims);
                    jwt.sign(new RSASSASigner("invalid-signature-code".equals(code) ? new RSAKeyGenerator(2048).generate() : KEY));
                    Map<String, Object> tokenResponse = new HashMap<>(Map.of("access_token", "test-access-token",
                            "token_type", "Bearer", "expires_in", 600, "id_token", jwt.serialize()));
                    if ("missing-id-token-code".equals(code)) tokenResponse.remove("id_token");
                    byte[] body = new ObjectMapper().writeValueAsBytes(tokenResponse);
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                } catch (Exception exception) {
                    TOKEN_FAILURES.incrementAndGet();
                    exchange.sendResponseHeaders(500, -1);
                } finally { exchange.close(); }
            });
            server.start();
            return server;
        } catch (Exception exception) { throw new IllegalStateException(exception); }
    }
}
