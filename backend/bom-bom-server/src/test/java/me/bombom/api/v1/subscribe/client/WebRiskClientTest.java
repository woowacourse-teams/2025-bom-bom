package me.bombom.api.v1.subscribe.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.queryParam;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;

import java.time.Duration;
import java.net.SocketTimeoutException;
import java.nio.charset.StandardCharsets;
import org.springframework.web.util.UriUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

class WebRiskClientTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final WebRiskClient client = new WebRiskClient(builder, "test-key", Duration.ofSeconds(3), Duration.ofSeconds(5));

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(client, "restClient", builder.baseUrl("https://webrisk.googleapis.com").build());
    }

    @Test
    void 원본_URL과_세_위협유형을_조회하고_빈_결과는_미탐지로_처리한다() {
        String url = "https://newsletter.example/leave?token=a%2Fb+c&email=user%40example.com";
        server.expect(queryParam("uri", UriUtils.encode(url, StandardCharsets.UTF_8)))
                .andExpect(queryParam("threatTypes", "SOCIAL_ENGINEERING", "MALWARE", "UNWANTED_SOFTWARE"))
                .andExpect(header("X-Goog-Api-Key", "test-key"))
                .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

        assertThat(client.isThreat(url)).isFalse();
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"SOCIAL_ENGINEERING", "MALWARE", "UNWANTED_SOFTWARE"})
    void 위협이_하나라도_탐지되면_차단한다(String threat) {
        server.expect(queryParam("uri", UriUtils.encode("https://newsletter.example/leave", StandardCharsets.UTF_8)))
                .andRespond(withSuccess("{\"threat\":{\"threatTypes\":[\"" + threat + "\"]}}", MediaType.APPLICATION_JSON));

        assertThat(client.isThreat("https://newsletter.example/leave")).isTrue();
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "null", "[]", "not-json", "{\"threat\":{}}", "{\"threat\":{\"threatTypes\":[]}}"})
    void 비정상_응답은_미탐지로_간주하지_않는다(String body) {
        server.expect(queryParam("uri", UriUtils.encode("https://newsletter.example/leave", StandardCharsets.UTF_8)))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> client.isThreat("https://newsletter.example/leave"))
                .isInstanceOf(WebRiskUnavailableException.class);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {302, 400, 403, 429, 500})
    void HTTP_오류는_민감정보_없이_검사실패로_처리한다(int status) {
        server.expect(queryParam("uri", UriUtils.encode("https://newsletter.example/leave?token=secret", StandardCharsets.UTF_8)))
                .andRespond(withStatus(HttpStatus.valueOf(status)));
        assertThatThrownBy(() -> client.isThreat("https://newsletter.example/leave?token=secret"))
                .isInstanceOf(WebRiskUnavailableException.class)
                .hasMessageNotContaining("secret")
                .hasMessageNotContaining("test-key")
                .hasNoCause();
        server.verify();
    }

    @Test
    void 시간초과는_검사실패로_처리한다() {
        server.expect(queryParam("uri", UriUtils.encode("https://newsletter.example/leave", StandardCharsets.UTF_8)))
                .andRespond(withException(new SocketTimeoutException("request timed out")));
        assertThatThrownBy(() -> client.isThreat("https://newsletter.example/leave"))
                .isInstanceOf(WebRiskUnavailableException.class)
                .hasNoCause();
        server.verify();
    }

    @Test
    void 키가_없으면_외부요청_없이_검사를_건너뛴다() {
        WebRiskClient unconfigured = new WebRiskClient(builder, "", Duration.ofSeconds(3), Duration.ofSeconds(5));
        assertThat(unconfigured.isThreat("https://newsletter.example/leave")).isFalse();
        server.verify();
    }
}
