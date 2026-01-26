package me.bombom.api.v1.badge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record RankingBadgeResponse(

        @NotNull
        BadgeGrade grade,

        @Schema(required = true)
        int year,

        @Schema(required = true)
        int month
) {

    public static RankingBadgeResponse from(MonthlyReadingRankFlat flat) {
        if (flat.hasRankingBadge()) {
            return new RankingBadgeResponse(
                    BadgeGrade.valueOf(flat.rankingBadgeGrade()),
                    flat.rankingBadgeYear(),
                    flat.rankingBadgeMonth()
            );
        }
        return null;
    }
}
