package me.bombom.api.v1.subscribe.scheduler;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.domain.UnsubscribeRetry;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import me.bombom.api.v1.subscribe.service.UnsubscribeRetryService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UnsubscribeRetryScheduler {

    public static final int RETRY_INTERVAL_MS = 300000; // 5분
    private static final int RETRY_BATCH_SIZE = 10;

    private final SubscribeService subscribeService;
    private final UnsubscribeRetryService unsubscribeRetryService;

    @Scheduled(fixedDelay = RETRY_INTERVAL_MS)
    @SchedulerLock(name = "retry_unsubscribe", lockAtLeastFor = "30s", lockAtMostFor = "4m")
    public void retryUnsubscribe() {
        List<UnsubscribeRetry> retries = unsubscribeRetryService.findPendingRetries(RETRY_BATCH_SIZE);
        if (retries.isEmpty()) {
            return;
        }
        log.info("재시도 대상 {}건 발견. 처리를 시작합니다.", retries.size());
        for (UnsubscribeRetry retry : retries) {
            try {
                processRetry(retry);
            } catch (Exception e) {
                log.error("재시도 처리 중 알 수 없는 오류 발생 - retryId: {}", retry.getId(), e);
            }
        }
    }

    private void processRetry(UnsubscribeRetry retry) {
        log.info("구독 취소 재시도 실행 - subscribeId: {}, retryCount: {}", retry.getSubscribeId(), retry.getRetryCount());
        subscribeService.retryUnsubscribe(retry.getSubscribeId());
    }
}
