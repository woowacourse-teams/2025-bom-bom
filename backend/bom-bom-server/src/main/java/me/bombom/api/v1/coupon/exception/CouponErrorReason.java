package me.bombom.api.v1.coupon.exception;

public enum CouponErrorReason {
    NOT_ACTIVE_SLOT,
    SOLD_OUT,
    EVENT_NOT_STARTED,
    EVENT_ENDED,
    ASSIGNMENT_RETRY_EXCEEDED,
    DUPLICATED_REQUEST
}
