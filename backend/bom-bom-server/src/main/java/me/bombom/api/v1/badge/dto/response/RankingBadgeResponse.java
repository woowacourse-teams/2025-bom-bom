package me.bombom.api.v1.badge.dto.response;

import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record RankingBadgeResponse(
        BadgeGrade grade,
        Integer year,
        Integer month
) {

    public static RankingBadgeResponse from(MonthlyReadingRankFlat flat) {
        if (flat.rankingBadgeGrade() == null) {
            return null;
        }
        return new RankingBadgeResponse(
                BadgeGrade.valueOf(flat.rankingBadgeGrade()),
                flat.rankingBadgeYear(),
                flat.rankingBadgeMonth()
        );
    }
}
