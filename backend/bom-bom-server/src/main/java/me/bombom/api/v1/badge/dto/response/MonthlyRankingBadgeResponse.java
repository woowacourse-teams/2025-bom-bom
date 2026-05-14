package me.bombom.api.v1.badge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;

public record MonthlyRankingBadgeResponse(

        @NotNull
        BadgeGrade grade,

        @Schema(required = true)
        int year,

        @Schema(required = true)
        int month
) {

    public static MonthlyRankingBadgeResponse from(MonthlyReadingRankFlat flat) {
        if (flat.hasRankingBadge()) {
            return new MonthlyRankingBadgeResponse(
                    BadgeGrade.valueOf(flat.rankingBadgeGrade()),
                    flat.rankingBadgeYear(),
                    flat.rankingBadgeMonth()
            );
        }
        return null;
    }

    public static MonthlyRankingBadgeResponse from(ContinueReadingRankFlat flat) {
        if (flat.hasRankingBadge()) {
            return new MonthlyRankingBadgeResponse(
                    BadgeGrade.valueOf(flat.rankingBadgeGrade()),
                    flat.rankingBadgeYear(),
                    flat.rankingBadgeMonth()
            );
        }
        return null;
    }
}
