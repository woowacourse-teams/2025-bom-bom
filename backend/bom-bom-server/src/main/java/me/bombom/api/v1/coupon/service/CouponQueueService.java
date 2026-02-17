package me.bombom.api.v1.coupon.service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.coupon.config.CouponQueueProperties;
import me.bombom.api.v1.coupon.config.CouponQueueProperties.Event;
import me.bombom.api.v1.coupon.domain.CouponIssue;
import me.bombom.api.v1.coupon.dto.response.CouponIssueSummaryResponse;
import me.bombom.api.v1.coupon.dto.response.CouponIssueResponse;
import me.bombom.api.v1.coupon.dto.response.CouponQueueStatus;
import me.bombom.api.v1.coupon.dto.response.CouponQueueStatusResponse;
import me.bombom.api.v1.coupon.exception.CouponErrorReason;
import me.bombom.api.v1.coupon.repository.CouponIssueRepository;
import me.bombom.api.v1.coupon.repository.CouponQueueRepository;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CouponQueueService {

    private final CouponQueueRepository couponQueueRepository;
    private final CouponIssueRepository couponIssueRepository;
    private final CouponQueueProperties couponQueueProperties;
    private final Clock clock;

    /**
     * 선착순 쿠폰 대기열에 현재 사용자를 등록합니다.
     * - 설정에 존재하지 않는 쿠폰 이름이면 예외를 발생시킵니다.
     * - 이미 대기열에 등록된 경우 예외를 발생시킵니다.
     */
    public CouponQueueStatusResponse registerQueue(String couponName, Member member) {
        Event event = getEventOrThrow(couponName);
        validateEventWindow(event, couponName, member.getId(), "registerQueue");
        Long memberId = member.getId();
        long nowMillis = clock.millis();

        couponQueueRepository.removeExpiredActive(couponName, nowMillis);
        long activeCount = couponQueueRepository.getActiveCount(couponName);

        if (isIssued(couponName, memberId)) {
            return buildStatus(event, couponName, CouponQueueStatus.ISSUED, null, activeCount, null, null);
        }
        if (couponQueueRepository.isActive(couponName, memberId)) {
            Long expireAt = couponQueueRepository.getActiveExpireAtMillis(couponName, memberId);
            Long expiresIn = expireAt != null ? Math.max(0L, (expireAt - nowMillis) / 1000) : null;
            return buildStatus(event, couponName, CouponQueueStatus.ACTIVE, null, activeCount, expiresIn, null);
        }
        Long rank = couponQueueRepository.rankQueue(couponName, memberId);
        long queueCount = couponQueueRepository.getQueueCount(couponName);
        boolean soldOut = couponQueueRepository.isSoldOut(couponName);
        if (rank != null) {
            return buildStatus(event, couponName, CouponQueueStatus.WAITING, rank + 1, activeCount, null, queueCount);
        }
        if (soldOut) {
            return buildStatus(event, couponName, CouponQueueStatus.SOLD_OUT, null, activeCount, null, null);
        }

        boolean added = couponQueueRepository.addIfAbsentQueue(couponName, memberId);
        if (added) {
            log.info("쿠폰 대기열 등록 성공 - couponName={}, memberId={}", couponName, memberId);
        }
        if (couponQueueRepository.isActive(couponName, memberId)) {
            Long expireAt = couponQueueRepository.getActiveExpireAtMillis(couponName, memberId);
            Long expiresIn = expireAt != null ? Math.max(0L, (expireAt - nowMillis) / 1000) : null;
            return buildStatus(event, couponName, CouponQueueStatus.ACTIVE, null, activeCount, expiresIn, null);
        }
        Long finalRank = couponQueueRepository.rankQueue(couponName, memberId);
        if (finalRank != null) {
            if (queueCount == 0L) {
                queueCount = couponQueueRepository.getQueueCount(couponName);
            }
            return buildStatus(event, couponName, CouponQueueStatus.WAITING, finalRank + 1, activeCount, null, queueCount);
        }

        return buildStatus(event, couponName, soldOut ? CouponQueueStatus.SOLD_OUT : CouponQueueStatus.NOT_IN_QUEUE, null, activeCount, null, null);
    }

    public CouponQueueStatusResponse getQueueStatus(String couponName, Member member) {
        Event event = getEventOrThrow(couponName);

        Long memberId = member.getId();
        long nowMillis = clock.millis();
        if (isBeforeStart(event, nowMillis)) {
            return buildStatus(event, couponName, CouponQueueStatus.NOT_IN_QUEUE, null, 0L, null, null);
        }
        if (isAfterEnd(event, nowMillis)) {
            return buildStatus(event, couponName, CouponQueueStatus.SOLD_OUT, null, 0L, null, null);
        }

        boolean wasActive = couponQueueRepository.isActive(couponName, memberId);
        couponQueueRepository.removeExpiredActive(couponName, nowMillis);
        long activeCount = couponQueueRepository.getActiveCount(couponName);

        if (isIssued(couponName, memberId)) {
            return buildStatus(event, couponName, CouponQueueStatus.ISSUED, null, activeCount, null, null);
        }

        boolean soldOut = couponQueueRepository.isSoldOut(couponName);

        if (couponQueueRepository.isActive(couponName, memberId)) {
            Long expireAt = couponQueueRepository.getActiveExpireAtMillis(couponName, memberId);
            Long expiresIn = expireAt != null ? Math.max(0L, (expireAt - nowMillis) / 1000) : null;
            return buildStatus(event, couponName, CouponQueueStatus.ACTIVE, null, activeCount, expiresIn, null);
        }

        Long rank = couponQueueRepository.rankQueue(couponName, memberId);
        if (rank != null) {
            long queueCount = couponQueueRepository.getQueueCount(couponName);
            return buildStatus(event, couponName, CouponQueueStatus.WAITING, rank + 1, activeCount, null, queueCount);
        }

        CouponQueueStatus status = soldOut ? CouponQueueStatus.SOLD_OUT : CouponQueueStatus.NOT_IN_QUEUE;
        return buildStatus(event, couponName, status, null, activeCount, null, null);
    }

    public void leaveQueue(String couponName, Member member) {
        getEventOrThrow(couponName);
        Long memberId = member.getId();
        couponQueueRepository.removeQueue(couponName, memberId);
        couponQueueRepository.removeActive(couponName, memberId);
        log.info("쿠폰 대기열 나가기 - couponName={}, memberId={}", couponName, memberId);
    }

    @Transactional
    public CouponIssueResponse issueCoupon(String couponName, Member member) {
        Event event = getEventOrThrow(couponName);

        Long memberId = member.getId();
        long nowMillis = clock.millis();

        validateEventWindow(event, couponName, memberId, "issueCoupon");
        couponQueueRepository.removeExpiredActive(couponName, nowMillis);

        if (!couponQueueRepository.isActive(couponName, memberId)) {
            throw new CIllegalArgumentException(ErrorDetail.PRECONDITION_FAILED)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "issueCoupon")
                    .addContext("couponName", couponName)
                    .addContext(ErrorContextKeys.REASON, CouponErrorReason.NOT_ACTIVE_SLOT.name());
        }

        if (couponIssueRepository.existsByMemberIdAndCouponName(memberId, couponName)) {
            throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "issueCoupon")
                    .addContext("couponName", couponName)
                    .addContext(ErrorContextKeys.REASON, CouponErrorReason.DUPLICATED_REQUEST.name());
        }

        if (couponQueueRepository.isSoldOut(couponName)) {
            couponQueueRepository.removeActive(couponName, memberId);
            couponQueueRepository.removeQueue(couponName, memberId);
            throw new CIllegalArgumentException(ErrorDetail.COUPON_SOLD_OUT)
                .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                .addContext(ErrorContextKeys.OPERATION, "issueCoupon")
                    .addContext("couponName", couponName)
                    .addContext(ErrorContextKeys.REASON, CouponErrorReason.SOLD_OUT.name());
        }

        CouponIssue savedIssue;
        try {
            savedIssue = assignCouponFromPoolWithRetry(couponName, memberId);
        } catch (DataIntegrityViolationException e) {
            CouponIssue issuedIssue = couponIssueRepository
                    .findTopByMemberIdAndCouponNameOrderByUpdatedAtDesc(memberId, couponName)
                    .orElse(null);
            if (issuedIssue != null) {
                savedIssue = issuedIssue;
            } else {
                throw new CIllegalArgumentException(ErrorDetail.DUPLICATED_DATA)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.OPERATION, "issueCoupon")
                        .addContext("couponName", couponName)
                        .addContext(ErrorContextKeys.REASON, CouponErrorReason.DUPLICATED_REQUEST.name());
            }
        }
        if (savedIssue == null) {
            couponQueueRepository.removeActive(couponName, memberId);
            couponQueueRepository.removeQueue(couponName, memberId);
            log.info("쿠폰 발급 예약 실패(수량 초과) - couponName={}, memberId={}", couponName, memberId);
            if (isSoldOutByPolicy(couponName, event)) {
                couponQueueRepository.markSoldOut(couponName);
                throw new CIllegalArgumentException(ErrorDetail.COUPON_SOLD_OUT)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.OPERATION, "issueCoupon")
                        .addContext("couponName", couponName)
                        .addContext(ErrorContextKeys.REASON, CouponErrorReason.SOLD_OUT.name());
            }
            throw new CIllegalArgumentException(ErrorDetail.PRECONDITION_FAILED)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "issueCoupon")
                    .addContext("couponName", couponName)
                    .addContext(ErrorContextKeys.REASON, CouponErrorReason.ASSIGNMENT_RETRY_EXCEEDED.name());
        }

        couponQueueRepository.addIssued(couponName, memberId);
        couponQueueRepository.increaseIssuedCount(couponName, 1L);
        couponQueueRepository.removeActive(couponName, memberId);
        couponQueueRepository.removeQueue(couponName, memberId);

        if (isSoldOutByPolicy(couponName, event)) {
            couponQueueRepository.markSoldOut(couponName);
        }

        log.info("쿠폰 발급 완료 - couponName={}, memberId={}, imageUrl={}", couponName, memberId, savedIssue.getImageUrl());
        return CouponIssueResponse.of(savedIssue.getImageUrl(), savedIssue.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public List<CouponIssueSummaryResponse> getIssuedCoupons(Member member) {
        return couponIssueRepository.findByMemberIdOrderByUpdatedAtDesc(member.getId()).stream()
                .map(issue -> CouponIssueSummaryResponse.of(
                        issue.getCouponName(),
                        issue.getImageUrl(),
                        issue.getUpdatedAt()
                ))
                .toList();
    }

    private CouponIssue assignCouponFromPoolWithRetry(String couponName, Long memberId) {
        final int maxRetryAttempts = 5;
        for (int i = 0; i < maxRetryAttempts; i++) {
            int updated = couponIssueRepository.assignAvailableIssueToMember(couponName, memberId);
            if (updated == 1) {
                return couponIssueRepository.findTopByMemberIdAndCouponNameOrderByUpdatedAtDesc(memberId, couponName)
                        .orElse(null);
            }
        }
        return null;
    }

    private Event getEventOrThrow(String couponName) {
        return couponQueueProperties.getEvents().stream()
                .filter(event -> couponName.equals(event.getName()))
                .findFirst()
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                        .addContext(ErrorContextKeys.OPERATION, "validateCouponConfigured")
                        .addContext("couponName", couponName));
    }

    private void validateEventWindow(Event event, String couponName, Long memberId, String operation) {
        LocalDateTime now = LocalDateTime.now(clock);
        if (event.getStartAt() != null && now.isBefore(event.getStartAt())) {
            throw new CIllegalArgumentException(ErrorDetail.EVENT_NOT_STARTED)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, operation)
                    .addContext("couponName", couponName)
                    .addContext(ErrorContextKeys.REASON, CouponErrorReason.EVENT_NOT_STARTED.name());
        }
        if (event.getEndAt() != null && now.isAfter(event.getEndAt())) {
            throw new CIllegalArgumentException(ErrorDetail.EVENT_ENDED)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, operation)
                    .addContext("couponName", couponName)
                    .addContext(ErrorContextKeys.REASON, CouponErrorReason.EVENT_ENDED.name());
        }
    }

    private boolean isBeforeStart(Event event, long nowMillis) {
        LocalDateTime now = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(nowMillis), clock.getZone());
        return event.getStartAt() != null && now.isBefore(event.getStartAt());
    }

    private boolean isAfterEnd(Event event, long nowMillis) {
        LocalDateTime now = LocalDateTime.ofInstant(java.time.Instant.ofEpochMilli(nowMillis), clock.getZone());
        return event.getEndAt() != null && now.isAfter(event.getEndAt());
    }

    private boolean isIssued(String couponName, Long memberId) {
        if (couponQueueRepository.isIssued(couponName, memberId)) {
            return true;
        }

        boolean exists = couponIssueRepository.existsByMemberIdAndCouponName(memberId, couponName);
        if (exists) {
            couponQueueRepository.addIssued(couponName, memberId);
        }
        return exists;
    }

    private boolean isSoldOutByPolicy(String couponName, Event event) {
        var stockCount = couponIssueRepository.getStockCountByCouponName(couponName);
        if (stockCount == null) {
            return true;
        }

        long issuedCount = stockCount.getIssuedCount() != null ? stockCount.getIssuedCount() : 0L;
        long availableCount = stockCount.getAvailableCount() != null ? stockCount.getAvailableCount() : 0L;
        long totalStock = Math.max(0L, issuedCount + availableCount);
        long effectiveMax = Math.min(event.getMaxCount(), totalStock);

        return issuedCount >= effectiveMax;
    }

    private CouponQueueStatusResponse buildStatus(
            Event event,
            String couponName,
            CouponQueueStatus status,
            Long position,
            Long activeCount,
            Long activeExpiresInSeconds,
            Long queueCount
    ) {
        int pollingTtlSeconds = resolvePollingTtlSeconds(status, event, position, queueCount);
        return CouponQueueStatusResponse.of(
                couponName,
                status,
                position,
                activeCount,
                activeExpiresInSeconds,
                pollingTtlSeconds
        );
    }

    private int resolvePollingTtlSeconds(CouponQueueStatus status, Event event, Long position, Long queueCount) {
        if (status != CouponQueueStatus.WAITING) {
            return event.getPollingIntervalSeconds();
        }
        if (position == null || position <= 0) {
            return event.getPollingIntervalSeconds();
        }
        long totalInQueue = queueCount != null ? queueCount : 0L;
        if (totalInQueue <= 1) {
            return 2;
        }
        long fromSecondToLast = 23L;
        long adjusted = position - 1;
        long indexFromFirst = Math.min(adjusted, totalInQueue - 1);
        long polling = 2L + (fromSecondToLast * indexFromFirst) / (totalInQueue - 1);
        if (polling > 25L) {
            return 25;
        }
        if (polling < 2L) {
            return 2;
        }
        return (int) polling;
    }
}
