package me.bombom.api.v1.subscribe.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.domain.SubscribeRetry;
import me.bombom.api.v1.subscribe.repository.SubscribeRepository;
import me.bombom.api.v1.subscribe.repository.SubscribeRetryRepository;
import me.bombom.api.v1.subscribe.service.SubscribeService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribeRetryScheduler {

    private final SubscribeRetryRepository subscribeRetryRepository;
    private final SubscribeRepository subscribeRepository;
    private final SubscribeService subscribeService;

    @Scheduled(fixedDelay = 60000) // 1분마다 실행
    public void retryUnsubscribe() {
        LocalDateTime now = LocalDateTime.now();
        List<SubscribeRetry> retries = subscribeRetryRepository.findByNextRetryAtBefore(now);

        if (!retries.isEmpty()) {
            log.info("재시도 대상 {}건 발견. 처리를 시작합니다.", retries.size());
        }

        for (SubscribeRetry retry : retries) {
            try {
                processRetry(retry);
            } catch (Exception e) {
                log.error("재시도 처리 중 알 수 없는 오류 발생 - retryId: {}", retry.getId(), e);
            }
        }
    }

    private void processRetry(SubscribeRetry retry) {
        subscribeRepository.findById(retry.getSubscribeId())
                .ifPresentOrElse(
                        subscribe -> {
                            log.info("구독 취소 재시도 실행 - subscribeId: {}, retryCount: {}",
                                    subscribe.getId(), retry.getRetryCount());
                            subscribeService.processUnsubscribe(
                                    subscribe.getId(),
                                    subscribe.getNewsletterId(),
                                    subscribe.getUnsubscribeUrl());
                        },
                        () -> {
                            log.warn("구독 정보가 존재하지 않아 재시도 항목 삭제 - subscribeId: {}", retry.getSubscribeId());
                            subscribeRetryRepository.delete(retry);
                        });
    }
}
