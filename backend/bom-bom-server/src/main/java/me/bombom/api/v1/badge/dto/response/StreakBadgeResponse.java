package me.bombom.api.v1.badge.dto.response;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.badge.domain.StreakBadgeTier;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record StreakBadgeResponse(

        @NotNull
        StreakBadgeTier tier
) {

    public static StreakBadgeResponse from(MonthlyReadingRankFlat flat) {
        if (flat.hasStreakBadge()) {
            return StreakBadgeTier.from(flat.streakDayCount())
                    .map(StreakBadgeResponse::new)
                    .orElse(null);
        }
        return null;
    }

    public static StreakBadgeResponse from(ContinueReadingRankFlat flat) {
        if (flat.hasStreakBadge()) {
            return StreakBadgeTier.from(flat.streakDayCount())
                    .map(StreakBadgeResponse::new)
                    .orElse(null);
        }
        return null;
    }
}
