package me.bombom.api.v1.subscribe.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;
import me.bombom.api.v1.subscribe.dto.response.PlaywrightResponse;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

class PlaywrightClientTest {

    private final LambdaClient lambdaClient = mock(LambdaClient.class);
    private final WebRiskClient webRiskClient = mock(WebRiskClient.class);
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final PlaywrightClient client = new PlaywrightClient(
            objectMapper, lambdaClient, webRiskClient);
    private final UnsubscribePatterns patterns = new UnsubscribePatterns("unsub", "success", "already", "error", List.of());

    @Test
    void 위협_URL은_람다를_호출하지_않는다() {
        String url = "https://phishing.example/unsubscribe";
        given(webRiskClient.isThreat(url)).willReturn(true);
        assertThat(client.executeUnsubscribe(url, patterns).statusCode()).isEqualTo(403);
        verifyNoInteractions(lambdaClient);
    }

    @Test
    void 검사_실패는_람다를_호출하지_않고_재시도_가능한_503을_반환한다() {
        String url = "https://newsletter.example/unsubscribe";
        given(webRiskClient.isThreat(url)).willThrow(new WebRiskUnavailableException());
        assertThat(client.executeUnsubscribe(url, patterns).statusCode()).isEqualTo(503);
        verifyNoInteractions(lambdaClient);
    }

    @Test
    void 허용_URL은_토큰과_패턴을_변경하지_않고_람다에_전달한다() throws Exception {
        ReflectionTestUtils.setField(client, "functionName", "test-unsubscribe");
        String url = "https://unsubscribe.example.com/leave?token=abc%2F123&email=a%2Bb%40example.com";
        given(lambdaClient.invoke(any(InvokeRequest.class))).willAnswer(invocation -> {
            InvokeRequest request = invocation.getArgument(0);
            assertThat(request.functionName()).isEqualTo("test-unsubscribe");
            var payload = objectMapper.readTree(request.payload().asUtf8String());
            assertThat(payload.get("url").asText()).isEqualTo(url);
            assertThat(payload.get("patterns").get("unsubscribe").asText()).isEqualTo("unsub");
            return InvokeResponse.builder()
                    .payload(SdkBytes.fromUtf8String("{\"statusCode\":200,\"success\":true,\"message\":\"Success\",\"method\":\"text_match\"}"))
                    .build();
        });

        assertThat(client.executeUnsubscribe(url, patterns).success()).isTrue();
    }
}
