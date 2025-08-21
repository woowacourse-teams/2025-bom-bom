package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberMonthlyReadingRankResponse(

        @Schema(required = true)
        long rank,

        @Schema(required = true)
        long totalMembers
) {
}
