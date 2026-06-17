package me.bombom.api.v1.member.dto.response;

import java.util.List;

public record RankSummaryResponse(
        List<RankCardResponse> cards
) {

    public static RankSummaryResponse from(List<RankCardResponse> cards) {
        return new RankSummaryResponse(cards);
    }
}
