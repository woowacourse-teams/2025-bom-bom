package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.dto.response.BadgesResponse;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;

public record MemberContinueReadingRankResponse(

        @NotNull
        String nickname,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        long rank,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int dayCount,

        BadgesResponse badges
) {

    public static MemberContinueReadingRankResponse from(ContinueReadingRankFlat flat) {
        return new MemberContinueReadingRankResponse(
                flat.nickname(),
                flat.rank(),
                flat.dayCount(),
                BadgesResponse.from(flat)
        );
    }
}
