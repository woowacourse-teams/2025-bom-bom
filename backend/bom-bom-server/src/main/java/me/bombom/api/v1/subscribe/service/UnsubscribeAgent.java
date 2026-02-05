package me.bombom.api.v1.subscribe.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.RetryableException;
import me.bombom.api.v1.subscribe.client.PlaywrightClient;
import me.bombom.api.v1.subscribe.config.SubscribePatternProperties;
import me.bombom.api.v1.subscribe.dto.UnsubscribePatterns;
import me.bombom.api.v1.subscribe.dto.response.PlaywrightResponse;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Component
@RequiredArgsConstructor
@Transactional(propagation = Propagation.NEVER)
public class UnsubscribeAgent {

    private final PlaywrightClient playwrightClient;
    private final SubscribePatternProperties properties;

    public void unsubscribe(String url, Long newsletterId) {
        UnsubscribePatterns patterns = new UnsubscribePatterns(
                properties.getUnsubscribePattern().pattern(),
                properties.getSuccessPattern().pattern(),
                properties.getAlreadyUnsubscribedPattern().pattern(),
                properties.getErrorPattern().pattern()
        );

        PlaywrightResponse response = playwrightClient.executeUnsubscribe(url, patterns);

        if (response.success()) {
            log.info("구독 취소 성공 - newsletterId: {}, method: {}", newsletterId, response.method());
        } else {
            String errorMsg = StringUtils.hasText(response.error()) ? response.error() : response.message();
            Integer statusCode = response.statusCode();
            log.warn("구독 취소 실패 - newsletterId: {}, status: {}, error: {}", newsletterId, statusCode, errorMsg);

            if (isRetryableError(statusCode)) {
                throw new RetryableException(errorMsg);
            }

            throw new AutoUnsubscribeFailedException(errorMsg, newsletterId, url);
        }
    }

    private boolean isRetryableError(Integer statusCode) {
        return statusCode != null && statusCode >= 500;
    }
}
