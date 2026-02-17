package me.bombom.api.v1.coupon.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record CouponQueueStatusResponse(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        String couponName,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        CouponQueueStatus status,

        @Schema(description = "대기열 순번(1부터 시작)")
        Long position,

        @Schema(description = "현재 입장 허용 인원")
        Long activeCount,

        @Schema(description = "입장 허용 만료까지 남은 시간(초)")
        Long activeExpiresInSeconds,

        @Schema(description = "권장 폴링 간격(초)")
        Integer pollingTtlSeconds
) {

    public static CouponQueueStatusResponse of(
            String couponName,
            CouponQueueStatus status,
            Long position,
            Long activeCount,
            Long activeExpiresInSeconds,
            Integer pollingTtlSeconds

    ) {
        return new CouponQueueStatusResponse(
                couponName,
                status,
                position,
                activeCount,
                activeExpiresInSeconds,
                pollingTtlSeconds
        );
    }
}
