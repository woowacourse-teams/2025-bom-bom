package me.bombom.api.v1.common;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class WebhookHttpClientTest {

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
    void 웹훅을_POST로_전송한다() {
        // given
        CapturedRequest capturedRequest = new CapturedRequest();
        server.createContext("/webhook", exchange -> {
            capturedRequest.method = exchange.getRequestMethod();
            capturedRequest.body = readBody(exchange);
            sendResponse(exchange, 204);
        });
        WebhookHttpClient webhookHttpClient = new WebhookHttpClient(RestClient.builder());

        // when
        webhookHttpClient.post(webhookUrl(), Map.of("content", "hello"));

        // then
        assertThat(capturedRequest.method).isEqualTo("POST");
        assertThat(capturedRequest.body).contains("\"content\":\"hello\"");
    }

    @Test
    void 웹훅_전송_실패는_외부로_전파하지_않는다() {
        // given
        server.createContext("/webhook", exchange -> sendResponse(exchange, 500));
        WebhookHttpClient webhookHttpClient = new WebhookHttpClient(RestClient.builder());

        // when & then
        assertThatCode(() -> webhookHttpClient.post(webhookUrl(), Map.of("content", "hello")))
                .doesNotThrowAnyException();
    }

    private String webhookUrl() {
        return "http://localhost:" + server.getAddress().getPort() + "/webhook";
    }

    private String readBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void sendResponse(HttpExchange exchange, int statusCode) throws IOException {
        exchange.sendResponseHeaders(statusCode, -1);
        exchange.close();
    }

    private static class CapturedRequest {

        private String method;
        private String body;
    }
}
