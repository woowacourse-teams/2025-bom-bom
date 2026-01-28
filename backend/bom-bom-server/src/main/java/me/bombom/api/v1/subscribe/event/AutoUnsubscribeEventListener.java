package me.bombom.api.v1.subscribe.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
<<<<<<< HEAD
import me.bombom.api.v1.subscribe.service.SubscribeService;
=======
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.common.exception.RetryableException;
import me.bombom.api.v1.subscribe.exception.AutoUnsubscribeFailedException;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import me.bombom.api.v1.subscribe.service.UnsubscribeAgent;
import org.springframework.context.ApplicationEventPublisher;
>>>>>>> 0080a940 (feat: 재시도 불가능 예외에 대해 디스코드 알림 추가)
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoUnsubscribeEventListener {

<<<<<<< HEAD
    private final SubscribeService subscribeService;
=======
    private final UnsubscribeAgent unsubscribeAgent;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscribeService subscribeService;
    private final DiscordWebhookNotifier discordWebhookNotifier;
>>>>>>> 0080a940 (feat: 재시도 불가능 예외에 대해 디스코드 알림 추가)

    @Async
    @TransactionalEventListener
    public void handleUnsubscribeRequested(UnsubscribeRequestedEvent event) {
        Long subscribeId = event.subscribeId();
        String unsubscribeUrl = event.unsubscribeUrl();
        Long newsletterId = event.newsletterId();

<<<<<<< HEAD
        log.info("구독 자동 취소 처리 시작 subscribeId: {}", subscribeId);
        subscribeService.processUnsubscribe(subscribeId, newsletterId, unsubscribeUrl);
        log.info("구독 자동 취소 처리 종료 subscribeId: {}", subscribeId);
=======
        try {
            log.info("구독 자동 취소 처리 시작 subscribeId: {}", subscribeId);
            boolean isSuccess = unsubscribeAgent.unsubscribe(unsubscribeUrl, newsletterId);
            eventPublisher.publishEvent(AutoUnsubscribeCompletedEvent.of(subscribeId, isSuccess));
            log.info("구독 자동 취소 처리 종료 subscribeId: {}", subscribeId);
        } catch (RetryableException e) {
            //재시도
        } catch (AutoUnsubscribeFailedException e) {
            discordWebhookNotifier.sendUnsubscribeErrorNotification(
                    e.getMessage(),
                    e.getNewsletterId(),
                    e.getUrl(),
                    subscribeId
            );
        } catch (Exception e) {
            log.error("예상치 못한 예외가 발생했습니다.", e);
        }
>>>>>>> 0080a940 (feat: 재시도 불가능 예외에 대해 디스코드 알림 추가)
    }

    @EventListener
    public void handleUnsubscribeCompleted(AutoUnsubscribeCompletedEvent event) {
        Long subscribeId = event.subscribeId();
        log.info("구독 자동 취소 결과 처리 시작 subscribeId: {}", subscribeId);
        subscribeService.handleUnsubscribeResult(subscribeId, event.isSuccess());
        log.info("구독 자동 취소 결과 처리 종료 subscribeId: {}", subscribeId);
    }
}
