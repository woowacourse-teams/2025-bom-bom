package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.dto.response.BadgesResponse;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record MemberMonthlyReadingRankResponse(

        @NotNull
        String nickname,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        long rank,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int monthlyReadCount,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        long nextRankDifference,

        BadgesResponse badges
) {

    public static MemberMonthlyReadingRankResponse from(MonthlyReadingRankFlat flat) {
        return new MemberMonthlyReadingRankResponse(
                flat.nickname(),
                flat.rank(),
                flat.monthlyReadCount(),
                flat.nextRankDifference(),
                BadgesResponse.from(flat));
    }
}
