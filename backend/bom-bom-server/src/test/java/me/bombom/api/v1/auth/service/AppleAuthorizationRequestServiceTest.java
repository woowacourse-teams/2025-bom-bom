package me.bombom.api.v1.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;
import me.bombom.api.v1.auth.dto.PendingAppleAuthorizationRequest;
import me.bombom.support.integration.IntegrationTest;
import me.bombom.support.time.MutableClock;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.oauth2.core.endpoint.OAuth2AuthorizationRequest;

@IntegrationTest
class AppleAuthorizationRequestServiceTest {

    @Autowired
    private AppleAuthorizationRequestService store;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private MutableClock clock;

    @AfterEach
    void cleanUp() {
        for (String key : java.util.List.of("a", "b", "c", "d")) {
            jdbcTemplate.update("DELETE FROM apple_authorization_request WHERE request_key = ?", key.repeat(64));
        }
    }

    @Test
    void 저장된_요청은_정확한_state와_유효기간에서만_조회한다() {
        Instant now = clock.instant();
        store.save("a".repeat(64), pending(), now.plusSeconds(300));

        assertThat(store.find("a".repeat(64), "state", now).authorizationRequest().getClientId()).isEqualTo("client");
        assertThat(store.find("a".repeat(64), "STATE", now)).isNull();
        assertThat(store.find("a".repeat(64), "state ", now)).isNull();
        assertThat(store.find("a".repeat(64), "wrong", now)).isNull();
        assertThat(store.find("a".repeat(64), "state", now.plusSeconds(300))).isNull();
    }

    @Test
    void 동시에_도착한_콜백은_하나만_소비한다() throws Exception {
        Instant now = clock.instant();
        String key = "b".repeat(64);
        store.save(key, pending(), now.plusSeconds(300));
        try (var executor = Executors.newFixedThreadPool(8)) {
            var ready = new CountDownLatch(8);
            var start = new CountDownLatch(1);
            var callbacks = IntStream.range(0, 8).mapToObj(index -> CompletableFuture.supplyAsync(() -> {
                ready.countDown();
                try {
                    if (!start.await(5, TimeUnit.SECONDS)) throw new IllegalStateException("start timeout");
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new IllegalStateException(exception);
                }
                return store.consume(key, now);
            }, executor)).toList();
            assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
            start.countDown();
            assertThat(callbacks.stream().map(CompletableFuture::join).filter(Boolean::booleanValue).count()).isEqualTo(1);
        }
        assertThat(store.find(key, "state", now)).isNull();
    }

    @Test
    void 새로운_요청을_저장할_때_만료된_데이터를_정리한다() {
        Instant now = clock.instant();
        store.save("c".repeat(64), pending(), now.plusSeconds(1));
        clock.setInstant(now.plusSeconds(2));
        store.save("d".repeat(64), pending(), clock.instant().plusSeconds(300));

        assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM apple_authorization_request", Integer.class)).isEqualTo(1);
    }

    private PendingAppleAuthorizationRequest pending() {
        var request = OAuth2AuthorizationRequest.authorizationCode()
                .authorizationUri("https://appleid.apple.com/auth/authorize").clientId("client")
                .redirectUri("https://api-dev.bombom.news/login/sso/verify/success/apple")
                .state("state").attributes(Map.of("registration_id", "apple")).build();
        return new PendingAppleAuthorizationRequest(request, Map.of("redirectUrl", "https://dev.bombom.news"));
    }
}
