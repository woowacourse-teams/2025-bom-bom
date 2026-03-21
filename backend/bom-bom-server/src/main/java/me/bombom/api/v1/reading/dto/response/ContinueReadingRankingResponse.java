package me.bombom.api.v1.reading.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ContinueReadingRankingResponse(

        @NotNull
        List<ContinueReadingRankResponse> data
) {

    public static ContinueReadingRankingResponse of(List<ContinueReadingRankResponse> data) {
        return new ContinueReadingRankingResponse(data);
    }
}
