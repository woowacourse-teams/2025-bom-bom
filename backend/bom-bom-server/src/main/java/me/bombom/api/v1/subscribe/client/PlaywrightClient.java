package me.bombom.api.v1.subscribe.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;
import me.bombom.api.v1.subscribe.dto.request.PlaywrightRequest;
import me.bombom.api.v1.subscribe.dto.response.PlaywrightResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PlaywrightClient {

    private final WebClient.Builder webClientBuilder;

    @Value("${aws.lambda.playwright.url}")
    private String lambdaUrl;

    public PlaywrightResponse executeUnsubscribe(String url, UnsubscribePatterns patterns) {
        PlaywrightRequest request = PlaywrightRequest.of(url, patterns);

        try {
            ResponseEntity<PlaywrightResponse> entity = webClientBuilder.build()
                    .post()
                    .uri(lambdaUrl)
                    .bodyValue(request)
                    .retrieve()
                    .toEntity(PlaywrightResponse.class)
                    .block(); // 동기 호출

            if (entity == null || entity.getBody() == null) {
                throw new RuntimeException("Lambda 응답이 없습니다.");
            }

            PlaywrightResponse body = entity.getBody();
            return new PlaywrightResponse(
                    entity.getStatusCode().value(),
                    body.success(),
                    body.message(),
                    body.error());

        } catch (WebClientResponseException e) { //서버 응답이 400 ~ 500인 경우
            log.error("Lambda 호출 실패 - status: {}, body: {}", e.getStatusCode(), e.getResponseBodyAsString());
            return new PlaywrightResponse(e.getStatusCode().value(), false, null, "Lambda 오류: " + e.getStatusCode());
        } catch (Exception e) {
            log.error("Lambda 호출 중 예외 발생", e);
            return new PlaywrightResponse(500, false, null, "내부 오류: " + e.getMessage());
        }
    }
}
