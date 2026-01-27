package me.bombom.api.v1.badge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record ChallengeBadgeResponse(

        @NotNull
        BadgeGrade grade,

        @NotNull
        String name,

        @Schema(required = true)
        int generation
) {

    public static ChallengeBadgeResponse from(MonthlyReadingRankFlat flat) {
        if (flat.hasChallengeBadge()) {
            return new ChallengeBadgeResponse(
                    BadgeGrade.valueOf(flat.challengeBadgeGrade()),
                    flat.challengeBadgeName(),
                    flat.challengeBadgeGeneration()
            );
        }
        return null;
    }
}
