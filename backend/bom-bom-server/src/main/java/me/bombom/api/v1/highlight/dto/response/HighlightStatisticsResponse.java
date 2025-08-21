package me.bombom.api.v1.highlight.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record HighlightStatisticsResponse(

        @Schema(type = "integer", format = "int32", description = "전체 하이라이트 수", required = true)
        int totalCount,

        @NotNull
        @Schema(type = "array", description = "뉴스레터 별 하이라이트 개수 통계", required = true)
        List<HighlightCountPerNewsletterResponse> newsletters
) {

    public static HighlightStatisticsResponse of(
            int totalCount,
            List<HighlightCountPerNewsletterResponse> countResponse
    ) {
        return new HighlightStatisticsResponse(totalCount, countResponse);
    }
}
