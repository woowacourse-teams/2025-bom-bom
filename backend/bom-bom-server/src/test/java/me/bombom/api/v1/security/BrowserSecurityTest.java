package me.bombom.api.v1.security;

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
import jakarta.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import me.bombom.api.v1.auth.dto.CustomOAuth2User;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.acceptance.AcceptanceTest;
import me.bombom.support.auth.FakeAppleOAuth2Service;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestComponent;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Disabled("수동 브라우저 보안 검증용. 실행할 때 이 애너테이션을 제거한다.")
@AcceptanceTest({"acceptance/common/member.json", "acceptance/member/member.json"})
@Import(BrowserSecurityTest.Fixtures.class)
@TestPropertySource(properties = {
        "app.security.csrf-enabled=true",
        "server.servlet.session.cookie.name=JSESSIONID_BROWSER_TEST",
        "server.servlet.session.cookie.domain=",
        "server.servlet.session.cookie.same-site=Lax",
        "server.forward-headers-strategy=framework"
})
class BrowserSecurityTest {

    private static final HttpServer IDP = startIdp();

    @Autowired private FakeAppleOAuth2Service appleService;
    @Autowired private MemberRepository memberRepository;

    @DynamicPropertySource
    static void provider(DynamicPropertyRegistry properties) {
        String base = "http://127.0.0.1:" + IDP.getAddress().getPort();
        properties.add("spring.security.oauth2.client.provider.apple.token-uri", () -> base + "/token");
        properties.add("spring.security.oauth2.client.provider.apple.jwk-set-uri", () -> base + "/jwks");
        properties.add("spring.security.oauth2.client.registration.apple.client-id", () -> "browser-apple-client");
        properties.add("spring.security.oauth2.client.registration.apple.scope", () -> "openid,name,email");
        properties.add("spring.security.oauth2.client.registration.apple.redirect-uri", () -> "{baseUrl}/login/sso/verify/success/apple");
    }

    @AfterAll
    static void stopIdp() { IDP.stop(0); }

    private static HttpServer startIdp() {
        try {
            RSAKey key = new RSAKeyGenerator(2048).keyID("browser-test-key").generate();
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.createContext("/jwks", exchange -> {
                byte[] body = new JWKSet(key.toPublicJWK()).toString().getBytes(StandardCharsets.UTF_8);
                exchange.getResponseHeaders().set("Content-Type", "application/json");
                exchange.sendResponseHeaders(200, body.length);
                exchange.getResponseBody().write(body);
                exchange.close();
            });
            server.createContext("/token", exchange -> {
                try {
                    String form = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    String code = java.util.Arrays.stream(form.split("&")).filter(part -> part.startsWith("code="))
                            .map(part -> URLDecoder.decode(part.substring(5), StandardCharsets.UTF_8)).findFirst().orElseThrow();
                    Instant now = Instant.now();
                    var claims = new JWTClaimsSet.Builder().issuer("https://appleid.apple.com")
                            .subject("browser-apple-subject").audience("browser-apple-client")
                            .issueTime(Date.from(now)).expirationTime(Date.from(now.plusSeconds(300)))
                            .claim("nonce", code).claim("email", "browser@example.test").build();
                    var jwt = new SignedJWT(new JWSHeader.Builder(JWSAlgorithm.RS256).keyID(key.getKeyID()).build(), claims);
                    jwt.sign(new RSASSASigner(key));
                    byte[] body = new ObjectMapper().writeValueAsBytes(Map.of("access_token", "browser-access-token",
                            "token_type", "Bearer", "expires_in", 300, "id_token", jwt.serialize()));
                    exchange.getResponseHeaders().set("Content-Type", "application/json");
                    exchange.sendResponseHeaders(200, body.length);
                    exchange.getResponseBody().write(body);
                } catch (Exception exception) {
                    exchange.sendResponseHeaders(500, -1);
                } finally { exchange.close(); }
            });
            server.start();
            return server;
        } catch (Exception exception) { throw new IllegalStateException(exception); }
    }

    @LocalServerPort
    private int port;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @Test
    void HTTPS_브라우저에서_쿠키_CORS_CSRF와_애플_POST를_검증한다() throws Exception {
        appleService.allowWebLogin(memberRepository.findById(1L).orElseThrow());
        for (String engine : System.getenv().getOrDefault("BOMBOM_SECURITY_BROWSERS", "chromium").split(",")) {
            runBrowser(engine.strip());
        }
    }

    private void runBrowser(String engine) throws Exception {
        Path script = Path.of("src/test/resources/security/security-browser-check.mjs").toAbsolutePath().normalize();
        Process process = new ProcessBuilder("node", script.toString(), String.valueOf(port),
                engine)
                .redirectError(ProcessBuilder.Redirect.INHERIT).start();
        var output = new BufferedReader(new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8));
        try {
            String ready = readLine(output);
            assertThat(ready).as("HTTPS proxy must start").isNotNull();
            int proxyPort = new ObjectMapper().readTree(ready).get("port").asInt();
            var source = (UrlBasedCorsConfigurationSource) corsConfigurationSource;
            source.getCorsConfigurations().get("/**").setAllowedOrigins(List.of("https://app.bombom.test:" + proxyPort));
            for (String callback : List.of("/login/sso/verify/success/apple", "/login/oauth2/code/apple")) {
                source.getCorsConfigurations().get(callback).setAllowedOrigins(List.of("https://idp.example.test:" + proxyPort));
            }
            process.getOutputStream().write("go\n".getBytes(StandardCharsets.UTF_8));
            process.getOutputStream().flush();
            process.getOutputStream().close();
            String result = readLine(output);
            assertThat(result).as("Browser must report completed checks").contains("\"passed\":true");
            assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM apple_authorization_request", Integer.class)).isZero();
            System.out.println(result);
        } finally {
            process.destroyForcibly();
            output.close();
        }
    }

    private String readLine(BufferedReader output) throws Exception {
        return java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return output.readLine();
            } catch (java.io.IOException exception) {
                throw new java.io.UncheckedIOException(exception);
            }
        }).get(45, TimeUnit.SECONDS);
    }

    @TestConfiguration(proxyBeanMethods = false)
    static class Fixtures {
        @Bean
        FixtureController browserLoginFixture(MemberRepository repository) {
            return new FixtureController(repository);
        }
    }

    @RestController
    @TestComponent
    static class FixtureController {
        private final MemberRepository repository;

        FixtureController(MemberRepository repository) {
            this.repository = repository;
        }

        @GetMapping("/__test/security/login")
        Map<String, Boolean> login(HttpSession session) {
            var member = repository.findById(1L).orElseThrow();
            var user = new CustomOAuth2User(Map.of("sub", "browser-test"), member, null, null);
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(new OAuth2AuthenticationToken(user, user.getAuthorities(), "google"));
            session.setAttribute("SPRING_SECURITY_CONTEXT", context);
            return Map.of("authenticated", true);
        }
    }
}
