package me.bombom.api.v1.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GetBookmarkNewsletterStatisticsResponse(
        @Schema(required = true)
        int totalCount,

        @NotNull
        List<GetBookmarkCountPerNewsletterResponse> newsletters
) {

    public static GetBookmarkNewsletterStatisticsResponse of(
            int totalCount,
            List<GetBookmarkCountPerNewsletterResponse> countResponse
    ) {
        return new GetBookmarkNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
