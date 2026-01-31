package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.dto.response.BadgesResponse;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record MemberMonthlyReadingRankResponse(

        @NotNull
        String nickname,

        @Schema(required = true)
        long rank,

        @Schema(required = true)
        int monthlyReadCount,

        @Schema(required = true)
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
