package me.bombom.api.v1.subscribe.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoUnsubscribeEventListener {

    private final SubscribeService subscribeService;

    @Async
    @TransactionalEventListener
    public void handleUnsubscribeRequested(UnsubscribeRequestedEvent event) {
        Long subscribeId = event.subscribeId();
        String unsubscribeUrl = event.unsubscribeUrl();
        Long newsletterId = event.newsletterId();

        log.info("구독 자동 취소 처리 시작 subscribeId: {}", subscribeId);
        subscribeService.processUnsubscribe(subscribeId, newsletterId, unsubscribeUrl);
        log.info("구독 자동 취소 처리 종료 subscribeId: {}", subscribeId);
    }

    @EventListener
    public void handleUnsubscribeCompleted(AutoUnsubscribeCompletedEvent event) {
        Long subscribeId = event.subscribeId();
        log.info("구독 자동 취소 결과 처리 시작 subscribeId: {}", subscribeId);
        subscribeService.handleUnsubscribeResult(subscribeId, event.isSuccess());
        log.info("구독 자동 취소 결과 처리 종료 subscribeId: {}", subscribeId);
    }
}
