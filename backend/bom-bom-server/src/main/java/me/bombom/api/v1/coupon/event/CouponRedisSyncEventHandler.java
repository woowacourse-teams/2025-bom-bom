package me.bombom.api.v1.coupon.event;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.coupon.repository.CouponQueueRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class CouponRedisSyncEventHandler {

    private final CouponQueueRepository couponQueueRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCouponIssued(CouponIssueCommittedEvent event) {
        couponQueueRepository.addIssued(event.couponName(), event.memberId());
        couponQueueRepository.increaseIssuedCount(event.couponName(), 1L);
        if (event.soldOut()) {
            couponQueueRepository.markSoldOut(event.couponName());
            return;
        }
        couponQueueRepository.clearSoldOut(event.couponName());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSoldOutDetected(CouponSoldOutDetectedEvent event) {
        couponQueueRepository.markSoldOut(event.couponName());
    }
}

