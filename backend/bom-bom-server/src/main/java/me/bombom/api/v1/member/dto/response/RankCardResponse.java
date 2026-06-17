package me.bombom.api.v1.member.dto.response;

import java.util.List;

public record RankCardResponse(
        String type,
        Long currentRank,
        List<RankHistoryResponse> rankHistory,
        int value
) {

    public static RankCardResponse of(
            String type,
            Long currentRank,
            List<RankHistoryResponse> rankHistory,
            int value
    ) {
        return new RankCardResponse(
                type,
                currentRank,
                rankHistory,
                value
        );
    }
}
