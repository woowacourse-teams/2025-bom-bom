package me.bombom.api.v1.common;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class InquiryWebhookRetrySenderTest {

    private HttpServer server;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
        server.start();
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void send는_5xx_응답이면_재시도_대상이므로_예외를_호출자에게_전파한다() {
        // given
        server.createContext("/webhook", exchange -> sendResponse(exchange, 500));
        InquiryWebhookRetrySender sender = new InquiryWebhookRetrySender(new WebhookHttpClient(RestClient.builder()));

        // when & then
        assertThatThrownBy(() -> sender.send(webhookUrl(), Map.of("content", "hello")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void send는_4xx_응답이어도_예외를_외부로_전파하지_않는다() {
        // given
        server.createContext("/webhook", exchange -> sendResponse(exchange, 400));
        InquiryWebhookRetrySender sender = new InquiryWebhookRetrySender(new WebhookHttpClient(RestClient.builder()));

        // when & then
        assertThatCode(() -> sender.send(webhookUrl(), Map.of("content", "hello")))
                .doesNotThrowAnyException();
    }

    private String webhookUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/webhook";
    }

    private void sendResponse(com.sun.net.httpserver.HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }
}
