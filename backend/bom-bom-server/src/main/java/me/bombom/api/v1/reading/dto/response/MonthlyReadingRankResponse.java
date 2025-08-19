package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record MonthlyReadingRankResponse(

        @NotNull
        Long memberId,

        @NotNull
        String nickname,

        @Schema(type = "integer", format = "int32", required = true)
        long rank,

        @Schema(type = "integer", format = "int32", required = true)
        int readCount
) {
}
