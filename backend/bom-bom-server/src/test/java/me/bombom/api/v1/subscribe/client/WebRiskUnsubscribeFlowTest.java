package me.bombom.api.v1.subscribe.client;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.anything;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import java.util.List;
import me.bombom.api.v1.common.exception.RetryableException;
import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import me.bombom.api.v1.subscribe.service.UnsubscribeAgent;
import me.bombom.api.v1.subscribe.service.UnsubscribePatternService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

class WebRiskUnsubscribeFlowTest {

    private final RestClient.Builder builder = RestClient.builder();
    private final MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();
    private final LambdaClient lambda = mock(LambdaClient.class);
    private final UnsubscribePatternService patterns = mock(UnsubscribePatternService.class);
    private final WebRiskClient risk = new WebRiskClient(builder, "test-key", Duration.ofSeconds(3), Duration.ofSeconds(5));
    private final PlaywrightClient playwright = new PlaywrightClient(new ObjectMapper(), lambda, risk);
    private final UnsubscribeAgent agent = new UnsubscribeAgent(playwright, patterns);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(risk, "restClient", builder.baseUrl("https://webrisk.googleapis.com").build());
        ReflectionTestUtils.setField(playwright, "functionName", "test-function");
        given(patterns.getPatterns()).willReturn(new UnsubscribePatterns("unsubscribe", "success", "already", "error", List.of()));
    }

    @ParameterizedTest
    @ValueSource(strings = {"SOCIAL_ENGINEERING", "MALWARE", "UNWANTED_SOFTWARE"})
    void 실제_검사에서_위협이_탐지되면_영구실패하고_람다는_호출하지_않는다(String threat) {
        server.expect(anything()).andRespond(withSuccess(
                "{\"threat\":{\"threatTypes\":[\"" + threat + "\"]}}", MediaType.APPLICATION_JSON));
        assertThatThrownBy(() -> agent.unsubscribe("https://newsletter.example/unsubscribe", 1L))
                .isInstanceOf(AutoUnsubscribeFailedException.class);
        verifyNoInteractions(lambda);
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(ints = {403, 429, 500, 503})
    void 검사서비스_오류는_구독취소_재시도_예외로_연결된다(int status) {
        server.expect(anything()).andRespond(withStatus(HttpStatus.valueOf(status)));
        assertThatThrownBy(() -> agent.unsubscribe("https://newsletter.example/unsubscribe", 1L))
                .isInstanceOf(RetryableException.class);
        verifyNoInteractions(lambda);
        server.verify();
    }

    @Test
    void 미탐지일_때만_람다를_실행하고_성공한다() {
        server.expect(anything()).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        given(lambda.invoke(any(InvokeRequest.class))).willReturn(InvokeResponse.builder()
                .payload(SdkBytes.fromUtf8String("{\"statusCode\":200,\"success\":true,\"message\":\"ok\"}"))
                .build());
        assertThatCode(() -> agent.unsubscribe("https://newsletter.example/unsubscribe", 1L))
                .doesNotThrowAnyException();
        verify(lambda).invoke(any(InvokeRequest.class));
        server.verify();
    }
}
