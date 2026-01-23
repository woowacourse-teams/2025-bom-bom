package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.dto.response.BadgesResponse;

public record MonthlyReadingRankResponse(

        @NotNull
        String nickname,

        @Schema(required = true)
        long rank,

        @Schema(required = true)
        int monthlyReadCount,

        BadgesResponse badges
) {

    public static MonthlyReadingRankResponse of(String nickname, long rank, int monthlyReadCount, BadgesResponse badges) {
        return new MonthlyReadingRankResponse(nickname, rank, monthlyReadCount, badges);
    }
}
