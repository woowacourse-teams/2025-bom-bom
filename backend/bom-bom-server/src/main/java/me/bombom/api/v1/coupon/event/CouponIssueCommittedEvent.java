package me.bombom.api.v1.coupon.event;

public record CouponIssueCommittedEvent(
        String couponName,
        Long memberId,
        boolean soldOut
) {
}

