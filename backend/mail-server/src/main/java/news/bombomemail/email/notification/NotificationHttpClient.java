package news.bombomemail.email.notification;

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
public class NotificationHttpClient {

    public void post(String url, Object body) {
        WebClient client = createWebClient();
        client.post()
                .uri(url)
                .bodyValue(body)
                .retrieve()
                .toBodilessEntity()
                .onErrorResume(e -> {
                    log.warn("[WARN] 알림 전송 실패: {}", e.toString());
                    return Mono.empty();
                })
                .subscribe();
    }

    private WebClient createWebClient() {
        ConnectionProvider provider = buildConnection();
        HttpClient httpClient = createHttpClient(provider);
        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .defaultHeader("Content-Type", "application/json")
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
        return ConnectionProvider.builder("notification-connection-pool")
                .maxConnections(10)
                .maxIdleTime(Duration.ofSeconds(10))
                .maxLifeTime(Duration.ofSeconds(30))
                .build();
    }
}
