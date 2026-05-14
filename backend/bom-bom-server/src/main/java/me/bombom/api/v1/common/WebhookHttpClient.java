package me.bombom.api.v1.common;

import java.time.Duration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
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

    private ClientHttpRequestFactory createRequestFactory() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(REQUEST_TIMEOUT);
        factory.setReadTimeout(REQUEST_TIMEOUT);
        return factory;
    }
}
