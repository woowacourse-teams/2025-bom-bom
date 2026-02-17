package me.bombom.api.v1.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CouponIssueResponse(

        @NotNull
        @Schema(description = "쿠폰 이미지 URL", requiredMode = Schema.RequiredMode.REQUIRED)
        String imageUrl,

        @NotNull
        @Schema(description = "발급 시간")
        LocalDateTime issuedAt
) {
    public static CouponIssueResponse of(String imageUrl, LocalDateTime issuedAt) {
        return new CouponIssueResponse(imageUrl, issuedAt);
    }
}
