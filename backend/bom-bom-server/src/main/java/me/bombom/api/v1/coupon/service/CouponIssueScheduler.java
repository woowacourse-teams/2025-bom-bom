package me.bombom.api.v1.coupon.service;

import java.time.Clock;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.coupon.config.CouponQueueProperties;
import me.bombom.api.v1.coupon.config.CouponQueueProperties.Event;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.coupon.repository.CouponQueueRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CouponIssueScheduler {

    private final CouponQueueRepository couponQueueRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponQueueProperties couponQueueProperties;
    private final Clock clock;

    /**
     * 선착순 쿠폰 발급 스케줄러.
     * <p>
     * - 1초마다 실행되며, 설정된 각 쿠폰 이벤트에 대해 대기열을 확인합니다.
     * - 이벤트별 start-at ~ end-at 시간 구간에만 발급 로직이 동작합니다.
     * - DB의 쿠폰 풀 발급 수량(issued/available)과 설정 max-count, active 인원 수를 기준으로 남은 슬롯을 계산하고
     *   선착순으로 일정 수(batch-size, 기본 50명)만큼씩 입장 허용합니다.
     * - 입장 허용된 사용자는 active TTL이 지나면 자동 만료됩니다.
     */
    @Scheduled(fixedDelay = 1000)
    @SchedulerLock(name = "couponIssueScheduler", lockAtMostFor = "PT5S")
    public void issue() {
        LocalDateTime now = LocalDateTime.now(clock);
        long nowMillis = clock.millis();

        for (Event event : couponQueueProperties.getEvents()) {
            String couponName = event.getName();
            long maxCount = event.getMaxCount();

            if (maxCount <= 0) {
                continue;
            }

            // 시간 윈도우 밖이면 스킵
            LocalDateTime startAt = event.getStartAt();
            LocalDateTime endAt = event.getEndAt();
            if (startAt != null && now.isBefore(startAt)) {
                couponQueueRepository.clearEventState(couponName);
                log.info("이벤트 시작 전 상태 초기화(클린업) - couponName={}", couponName);
                continue;
            }
            if (endAt != null && now.isAfter(endAt)) {
                couponQueueRepository.clearEventState(couponName);
                log.info("이벤트 종료 후 상태 초기화(클린업) - couponName={}", couponName);
                continue;
            }

            couponQueueRepository.removeExpiredActive(couponName, nowMillis);
            if (couponQueueRepository.isSoldOut(couponName)) {
                log.info("쿠폰 대기열 처리 스킵(소진) - couponName={}", couponName);
                continue;
            }

            long issuedCount = couponIssueRepository.countByCouponNameAndMemberIdIsNotNull(couponName);
            long availableCount = couponIssueRepository.countByCouponNameAndMemberIdIsNull(couponName);
            long totalStock = Math.max(0L, issuedCount + availableCount);
            long effectiveMax = Math.min(maxCount, totalStock);

            long cachedIssuedCount = couponQueueRepository.getIssuedCount(couponName);
            if (cachedIssuedCount != issuedCount) {
                couponQueueRepository.increaseIssuedCount(couponName, issuedCount - cachedIssuedCount);
            }
            long activeCount = couponQueueRepository.getActiveCount(couponName);
            long queueCount = couponQueueRepository.getQueueCount(couponName);
            long remainingSlots = effectiveMax - issuedCount - activeCount;
            if (remainingSlots <= 0) {
                couponQueueRepository.markSoldOut(couponName);
                log.info("쿠폰 대기열 처리 스킵(슬롯 없음) - couponName={}, queueCount={}, activeCount={}, issuedCount={}, maxCount={}, totalStock={}",
                        couponName, queueCount, activeCount, issuedCount, maxCount, totalStock);
                continue;
            }

            long activeLimit = event.getActiveLimit() > 0 ? event.getActiveLimit() : 50L;
            long availableSlots = activeLimit - activeCount;
            if (availableSlots <= 0) {
                log.info("쿠폰 대기열 처리 스킵(입장 허용 가득 참) - couponName={}, queueCount={}, activeCount={}, issuedCount={}",
                        couponName, queueCount, activeCount, issuedCount);
                continue;
            }

            long configuredBatchSize = event.getBatchSize() > 0 ? event.getBatchSize() : 50L;
            long batchSize = Math.min(configuredBatchSize, Math.min(availableSlots, remainingSlots));
            if (batchSize <= 0) {
                continue;
            }

            long activeTtlSeconds = event.getActiveTtlSeconds() > 0 ? event.getActiveTtlSeconds() : 30L;
            long expireAt = nowMillis + (activeTtlSeconds * 1000L);

            long promotedCount = couponQueueRepository.promoteQueueToActive(couponName, batchSize, expireAt);
            if (promotedCount > 0) {
                log.info("쿠폰 입장 허용 - couponName={}, promotedCount={}, queueCount={}, activeCount={}, issuedCount={}, maxCount={}, totalStock={}, expireAt={}",
                        couponName, promotedCount, queueCount, activeCount, issuedCount, maxCount, totalStock, expireAt);
            }
        }
    }
}
