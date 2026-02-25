package me.bombom.api.v1.coupon.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.coupon.domain.CouponIssue;

public record CouponIssueSummaryResponse(

        @NotNull
        String couponName,

        @NotNull
        String imageUrl,

        @NotNull
        LocalDateTime issuedAt
) {
    public static CouponIssueSummaryResponse of(CouponIssue issue) {
        return new CouponIssueSummaryResponse(
                issue.getCouponName(),
                issue.getImageUrl(),
                issue.getUpdatedAt()
        );
    }

    public static List<CouponIssueSummaryResponse> of(List<CouponIssue> issues) {
        return issues.stream()
                .map(CouponIssueSummaryResponse::of)
                .toList();
    }
}
