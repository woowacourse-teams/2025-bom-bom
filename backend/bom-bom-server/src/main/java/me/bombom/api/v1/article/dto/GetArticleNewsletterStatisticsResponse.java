package me.bombom.api.v1.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GetArticleNewsletterStatisticsResponse(
        @Schema(required = true)
        int totalCount,

        @NotNull
        List<GetArticleCountPerNewsletterResponse> newsletters
) {

    public static GetArticleNewsletterStatisticsResponse of(
            int totalCount,
            List<GetArticleCountPerNewsletterResponse> countResponse
    ) {
        return new GetArticleNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
