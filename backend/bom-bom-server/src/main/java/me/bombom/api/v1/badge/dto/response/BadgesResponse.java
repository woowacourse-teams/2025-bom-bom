package me.bombom.api.v1.badge.dto.response;

import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;

public record BadgesResponse(
        MonthlyRankingBadgeResponse monthlyRanking,
        ChallengeBadgeResponse challenge,
        StreakBadgeResponse streak
) {

    public static BadgesResponse from(MonthlyReadingRankFlat flat) {
        MonthlyRankingBadgeResponse monthlyRanking = MonthlyRankingBadgeResponse.from(flat);
        ChallengeBadgeResponse challenge = ChallengeBadgeResponse.from(flat);
        StreakBadgeResponse streak = StreakBadgeResponse.from(flat);
        if (monthlyRanking == null && challenge == null && streak == null) {
            return null;
        }
        return new BadgesResponse(monthlyRanking, challenge, streak);
    }

    public static BadgesResponse from(ContinueReadingRankFlat flat) {
        MonthlyRankingBadgeResponse monthlyRanking = MonthlyRankingBadgeResponse.from(flat);
        ChallengeBadgeResponse challenge = ChallengeBadgeResponse.from(flat);
        StreakBadgeResponse streak = StreakBadgeResponse.from(flat);
        if (monthlyRanking == null && challenge == null && streak == null) {
            return null;
        }
        return new BadgesResponse(monthlyRanking, challenge, streak);
    }
}
