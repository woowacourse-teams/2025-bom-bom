package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record RankCardResponse(

        @NotNull
        String type,

        Long currentRank,

        @NotNull
        List<RankHistoryResponse> rankHistory,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
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
