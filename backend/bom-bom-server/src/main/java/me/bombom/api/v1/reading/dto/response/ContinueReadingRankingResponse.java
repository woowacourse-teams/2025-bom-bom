package me.bombom.api.v1.reading.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record ContinueReadingRankingResponse(

        @NotNull
        LocalDateTime rankingUpdatedAt,

        @NotNull
        LocalDateTime nextRefreshAt,

        @NotNull
        LocalDateTime serverTime,

        @NotNull
        List<ContinueReadingRankResponse> data
) {

    public static ContinueReadingRankingResponse of(
            LocalDateTime rankingUpdatedAt,
            LocalDateTime nextRefreshAt,
            LocalDateTime serverTime,
            List<ContinueReadingRankResponse> data
    ) {
        return new ContinueReadingRankingResponse(rankingUpdatedAt, nextRefreshAt, serverTime, data);
    }
}
