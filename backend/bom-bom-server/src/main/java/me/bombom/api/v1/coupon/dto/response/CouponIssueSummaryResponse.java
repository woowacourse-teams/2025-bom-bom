package me.bombom.api.v1.coupon.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CouponIssueSummaryResponse(

        @NotNull
        String couponName,

        @NotNull
        String imageUrl,

        @NotNull
        LocalDateTime issuedAt
) {
    public static CouponIssueSummaryResponse of(String couponName, String imageUrl, LocalDateTime issuedAt) {
        return new CouponIssueSummaryResponse(couponName, imageUrl, issuedAt);
    }
}
