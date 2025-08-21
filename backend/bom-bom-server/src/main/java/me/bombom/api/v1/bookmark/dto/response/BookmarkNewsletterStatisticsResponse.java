package me.bombom.api.v1.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record BookmarkNewsletterStatisticsResponse(

        @Schema(required = true)
        int totalCount,

        @NotNull
        List<BookmarkCountPerNewsletterResponse> newsletters
) {

    public static BookmarkNewsletterStatisticsResponse of(
            int totalCount,
            List<BookmarkCountPerNewsletterResponse> countResponse
    ) {
        return new BookmarkNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
