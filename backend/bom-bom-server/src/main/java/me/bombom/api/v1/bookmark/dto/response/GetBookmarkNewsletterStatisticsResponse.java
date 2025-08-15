package me.bombom.api.v1.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GetBookmarkNewsletterStatisticsResponse(
        @Schema(type = "integer", format = "int32", description = "전체 북마크 수", required = true)
        int totalCount,

        @NotNull
        @Schema(type = "array", description = "뉴스레터별 통계", required = true)
        List<GetBookmarkCountPerNewsletterResponse> newsletters
) {

    public static GetBookmarkNewsletterStatisticsResponse of(
            int totalCount,
            List<GetBookmarkCountPerNewsletterResponse> countResponse
    ) {
        return new GetBookmarkNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
