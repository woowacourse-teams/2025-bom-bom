package me.bombom.api.v1.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GetArticleNewsletterStatisticsResponse(
        @Schema(type = "integer", format = "int32", description = "전체 아티클 수", required = true)
        int totalCount,

        @NotNull
        @Schema(type = "array", description = "뉴스레터별 통계", required = true)
        List<GetArticleCountPerNewsletterResponse> newsletters
) {

    public static GetArticleNewsletterStatisticsResponse of(
            int totalCount,
            List<GetArticleCountPerNewsletterResponse> countResponse
    ) {
        return new GetArticleNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
