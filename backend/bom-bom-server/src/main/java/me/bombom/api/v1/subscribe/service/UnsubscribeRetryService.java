package me.bombom.api.v1.subscribe.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.subscribe.domain.UnsubscribeRetry;
import me.bombom.api.v1.subscribe.repository.UnsubscribeRetryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UnsubscribeRetryService {

    private final UnsubscribeRetryRepository unsubscribeRetryRepository;
    private final Clock clock;

    @Transactional
    public boolean scheduleRetry(Long subscribeId, String errorMsg) {
        Optional<UnsubscribeRetry> existingRetry = unsubscribeRetryRepository.findBySubscribeId(subscribeId);
        LocalDateTime now = LocalDateTime.now(clock);

        if (existingRetry.isEmpty()) {
            UnsubscribeRetry newRetry = UnsubscribeRetry.builder()
                    .subscribeId(subscribeId)
                    .nextRetryAt(now)
                    .lastError(errorMsg)
                    .build();
            newRetry.increaseRetryCount(now, errorMsg);
            unsubscribeRetryRepository.save(newRetry);

            log.info("구독 취소 {}번째 재시도 예약 - subscribeId: {}, next: {}", newRetry.getRetryCount(), subscribeId,
                    newRetry.getNextRetryAt());
            return true;
        }

        UnsubscribeRetry retry = existingRetry.get();
        if (retry.isMaxRetryReached()) {
            log.error("구독 취소 재시도 횟수 초과 - subscribeId: {}", subscribeId);
            unsubscribeRetryRepository.delete(retry);
            return false;
        }

        retry.increaseRetryCount(now, errorMsg);
        log.info("구독 취소 {}번째 재시도 예약 - subscribeId: {}, next: {}", retry.getRetryCount(), subscribeId, retry.getNextRetryAt());
        return true;
    }

    @Transactional
    public void deleteIfExists(Long subscribeId) {
        unsubscribeRetryRepository.findBySubscribeId(subscribeId)
                .ifPresent(unsubscribeRetryRepository::delete);
    }

    @Transactional
    public void delete(UnsubscribeRetry retry) {
        unsubscribeRetryRepository.delete(retry);
    }

    public List<UnsubscribeRetry> findPendingRetries(int limit) {
        return unsubscribeRetryRepository.findPendingRetries(LocalDateTime.now(clock), limit);
    }
}
