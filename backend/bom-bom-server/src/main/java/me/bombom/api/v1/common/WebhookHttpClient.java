package me.bombom.api.v1.common;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebhookHttpClient {

    public void post(String url, Object body) {
        WebClient client = createWebClient();
        client.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> {
                    log.warn("[WARN] Webhook 전송 실패: {}", e);
                    return Mono.empty();
                })
                .subscribe();
    }

    private WebClient createWebClient() {
        ConnectionProvider provider = buildConnection();
        HttpClient httpClient = createHttpClient(provider);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }

    private HttpClient createHttpClient(ConnectionProvider provider) {
        return HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .responseTimeout(Duration.ofSeconds(5))
                .doOnConnected(conn -> conn
                        .addHandlerLast(new ReadTimeoutHandler(5, TimeUnit.SECONDS))
                        .addHandlerLast(new WriteTimeoutHandler(5, TimeUnit.SECONDS)))
                .keepAlive(true)
                .compress(true);
    }

    private ConnectionProvider buildConnection() {
        return ConnectionProvider.builder("webhook-connection-pool")
                .maxIdleTime(Duration.ofSeconds(10))   // 10초 유휴 시 커넥션 닫기
                .maxLifeTime(Duration.ofSeconds(30))   // 30초 지나면 새 커넥션 생성
                .build();
    }
}
