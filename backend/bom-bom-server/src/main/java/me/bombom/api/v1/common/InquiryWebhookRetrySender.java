package me.bombom.api.v1.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;

@Slf4j
@Component
@RequiredArgsConstructor
public class InquiryWebhookRetrySender {

    private final WebhookHttpClient webhookClient;

    // 재시도 대상: ResourceAccessException(타임아웃/연결 실패), HttpServerErrorException(5xx)
    @Retryable(
            retryFor = {ResourceAccessException.class, HttpServerErrorException.class},
            maxAttempts = 2, // 최초 호출 포함 2번 시도 (재시도 1번)
            backoff = @Backoff(delay = 500, random = true)
    )
    public void send(String url, Object body) {
        try {
            webhookClient.postSync(url, body);
        } catch (ResourceAccessException | HttpServerErrorException e) {
            throw e; // 재시도 대상 - @Retryable이 재시도하도록 그대로 전파한다.
        } catch (HttpClientErrorException e) {
            log.error("[ERROR] 문의 웹훅 요청이 거부되었습니다 (재시도 대상 아님): url={}, status={}",
                    url, e.getStatusCode(), e);
        } catch (Exception e) {
            log.error("[ERROR] 문의 웹훅 전송 중 예상치 못한 예외가 발생했습니다 (재시도 대상 아님): url={}", url, e);
        }
    }

    @Recover
    public void recover(Exception e, String url, Object body) {
        log.error("[ERROR] 문의 웹훅 전송 실패 (재시도 소진), 관리자 확인 필요: url={}", url, e);
    }
}
