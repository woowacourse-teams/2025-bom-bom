package me.bombom.api.v1.badge.dto.response;

import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;

public record BadgesResponse(
        RankingBadgeResponse ranking,
        ChallengeBadgeResponse challenge
) {

    public static BadgesResponse from(MonthlyReadingRankFlat flat) {
        RankingBadgeResponse ranking = RankingBadgeResponse.from(flat);
        ChallengeBadgeResponse challenge = ChallengeBadgeResponse.from(flat);
        if (ranking == null && challenge == null) {
            return null;
        }
        return new BadgesResponse(ranking, challenge);
    }
}
