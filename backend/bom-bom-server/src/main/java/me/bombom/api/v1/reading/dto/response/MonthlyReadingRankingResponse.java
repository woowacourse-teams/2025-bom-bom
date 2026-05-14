package me.bombom.api.v1.reading.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record MonthlyReadingRankingResponse(

        @NotNull
        LocalDateTime rankingUpdatedAt,

        @NotNull
        LocalDateTime nextRefreshAt,

        @NotNull
        LocalDateTime serverTime,

        @NotNull
        List<MonthlyReadingRankResponse> data
) {

    public static MonthlyReadingRankingResponse of(
            LocalDateTime rankingUpdatedAt,
            LocalDateTime nextRefreshAt,
            LocalDateTime serverTime,
            List<MonthlyReadingRankResponse> data
    ) {
        return new MonthlyReadingRankingResponse(rankingUpdatedAt, nextRefreshAt, serverTime, data);
    }
}
