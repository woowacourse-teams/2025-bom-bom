package me.bombom.api.v1.member.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RankSummaryResponse(

        @NotNull
        List<RankCardResponse> cards
) {

    public static RankSummaryResponse from(List<RankCardResponse> cards) {
        return new RankSummaryResponse(cards);
    }
}
