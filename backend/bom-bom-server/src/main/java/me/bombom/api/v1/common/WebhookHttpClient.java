package me.bombom.api.v1.common;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Slf4j
@Component
public class WebhookHttpClient {

    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(5);

    private final RestClient restClient;

    public WebhookHttpClient(RestClient.Builder restClientBuilder) {
        this.restClient = restClientBuilder
                .requestFactory(createRequestFactory())
                .build();
    }

    @Async
    public void post(String url, Object body) {
        try {
            restClient.post()
                    .uri(url)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();
        } catch (Exception e) {
            log.warn("[WARN] Webhook 전송 실패: {}", e.getMessage(), e);
        }
    }

    // 동기 호출 - 호출자의 스레드에서 재시도까지 수행되며, 실패 시 예외를 호출자에게 그대로 전파한다.
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 2, // 최초 호출 포함 2번 시도 (재시도 1번)
            backoff = @Backoff(delay = 500, random = true)
    )
    public void postWithRetry(String url, Object body) {
        restClient.post()
                .uri(url)
                .body(body)
                .retrieve()
                .toBodilessEntity();
    }

    @Recover
    public void recoverPost(Exception e, String url, Object body) {
        throw (e instanceof RuntimeException re) ? re : new IllegalStateException(e);
    }

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(REQUEST_TIMEOUT);
        factory.setReadTimeout(REQUEST_TIMEOUT);
        return factory;
    }
}
