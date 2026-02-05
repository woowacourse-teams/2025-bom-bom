package me.bombom.api.v1.subscribe.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;
import me.bombom.api.v1.subscribe.dto.request.PlaywrightRequest;
import me.bombom.api.v1.subscribe.dto.response.PlaywrightResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.SdkBytes;
import software.amazon.awssdk.services.lambda.LambdaClient;
import software.amazon.awssdk.services.lambda.model.InvokeRequest;
import software.amazon.awssdk.services.lambda.model.InvokeResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaywrightClient {

    private final ObjectMapper objectMapper;
    private final LambdaClient lambdaClient;

    @Value("${aws.lambda.playwright.function-name}")
    private String functionName;

    public PlaywrightResponse executeUnsubscribe(String url, UnsubscribePatterns patterns) {
        PlaywrightRequest requestBody = PlaywrightRequest.of(url, patterns);
        try {
            String payload = objectMapper.writeValueAsString(requestBody);
            InvokeRequest invokeRequest = InvokeRequest.builder()
                    .functionName(functionName)
                    .payload(SdkBytes.fromUtf8String(payload))
                    .build();

            InvokeResponse response = lambdaClient.invoke(invokeRequest);

            if (response.functionError() != null) {
                log.error("Lambda 실행 오류: {}", response.functionError());
                return new PlaywrightResponse(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        false,
                        null,
                        "Lambda 실행 오류: " + response.functionError()
                );
            }
            String responsePayload = response.payload().asUtf8String();
            return objectMapper.readValue(responsePayload, PlaywrightResponse.class);
        } catch (Exception e) {
            log.error("Lambda 호출 중 예외 발생", e);
            return new PlaywrightResponse(
                    HttpStatus.INTERNAL_SERVER_ERROR.value(),
                    false,
                    null,
                    "내부 오류: " + e.getMessage()
            );
        }
    }
}
