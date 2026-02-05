package me.bombom.api.v1.subscribe.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.domain.SubscribeRetry;
import me.bombom.api.v1.subscribe.repository.SubscribeRetryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscribeRetryService {

    private final SubscribeRetryRepository subscribeRetryRepository;
    private final Clock clock;

    @Transactional
    public boolean scheduleRetry(Long subscribeId, String errorMsg) {
        Optional<SubscribeRetry> existingRetry = subscribeRetryRepository.findBySubscribeId(subscribeId);
        LocalDateTime now = LocalDateTime.now(clock);

        if (existingRetry.isEmpty()) {
            SubscribeRetry newRetry = SubscribeRetry.builder()
                    .subscribeId(subscribeId)
                    .nextRetryAt(now)
                    .lastError(errorMsg)
                    .build();
            newRetry.increaseRetryCount(now, errorMsg);
            subscribeRetryRepository.save(newRetry);

            log.info("구독 취소 {}번째 재시도 예약 - subscribeId: {}, next: {}", newRetry.getRetryCount(), subscribeId, newRetry.getNextRetryAt());
            return true;
        }

        SubscribeRetry retry = existingRetry.get();
        if (retry.isMaxRetryReached()) {
            log.error("구독 취소 재시도 횟수 초과 - subscribeId: {}", subscribeId);
            subscribeRetryRepository.delete(retry);
            return false;
        }

        retry.increaseRetryCount(now, errorMsg);
        log.info("구독 취소 {}번째 재시도 예약 - subscribeId: {}, next: {}", retry.getRetryCount(), subscribeId, retry.getNextRetryAt());
        return true;
    }

    @Transactional
    public void deleteIfExists(Long subscribeId) {
        subscribeRetryRepository.findBySubscribeId(subscribeId)
                .ifPresent(subscribeRetryRepository::delete);
    }

    @Transactional
    public void delete(SubscribeRetry retry) {
        subscribeRetryRepository.delete(retry);
    }

    public List<SubscribeRetry> findPendingRetries(int limit) {
        return subscribeRetryRepository.findPendingRetries(LocalDateTime.now(clock), limit);
    }
}
