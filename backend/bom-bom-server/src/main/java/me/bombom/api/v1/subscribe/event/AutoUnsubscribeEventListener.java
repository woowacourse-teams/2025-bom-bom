package me.bombom.api.v1.subscribe.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import me.bombom.api.v1.subscribe.service.UnsubscribeAgent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class AutoUnsubscribeEventListener {

    private final UnsubscribeAgent unsubscribeAgent;
    private final ApplicationEventPublisher eventPublisher;
    private final SubscribeService subscribeService;

    @Async
    @TransactionalEventListener
    public void handleUnsubscribeRequested(UnsubscribeRequestedEvent event) {
        Long subscribeId = event.subscribeId();
        String unsubscribeUrl = event.unsubscribeUrl();

        log.info("구독 자동 취소 시작 subscribeId: {}", subscribeId);
        boolean isSuccess = unsubscribeAgent.unsubscribe(unsubscribeUrl);

        eventPublisher.publishEvent(AutoUnsubscribeCompletedEvent.of(subscribeId, isSuccess));
        log.info("구독 자동 취소 처리 완료 subscribeId: {}", subscribeId);
    }

    @Transactional
    @EventListener
    public void handleUnsubscribeCompleted(AutoUnsubscribeCompletedEvent event) {
        Long subscribeId = event.subscribeId();
        log.info("구독 자동 취소 완료 처리 시작 subscribeId: {}", subscribeId);
        subscribeService.handleUnsubscribeResult(subscribeId, event.isSuccess());
        log.info("구독 자동 취소 완료 처리 종료 subscribeId: {}", subscribeId);
    }
}
