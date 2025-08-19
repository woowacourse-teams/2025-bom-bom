package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MonthlyReadingRankResponse(

        @NotNull
        String nickname,

        @Schema(required = true)
        int rank,

        @Schema(required = true)
        int monthlyReadCount,

        @Schema(required = true)
        int weeklyReadCount
) {
}
