package me.bombom.api.v1.badge.dto.response;

import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record ChallengeBadgeResponse(
        BadgeGrade grade,
        String name,
        Integer generation
) {

    public static ChallengeBadgeResponse from(MonthlyReadingRankFlat flat) {
        if (flat.challengeBadgeGrade() == null) {
            return null;
        }
        return new ChallengeBadgeResponse(
                BadgeGrade.valueOf(flat.challengeBadgeGrade()),
                flat.challengeBadgeName(),
                flat.challengeBadgeGeneration()
        );
    }
}
