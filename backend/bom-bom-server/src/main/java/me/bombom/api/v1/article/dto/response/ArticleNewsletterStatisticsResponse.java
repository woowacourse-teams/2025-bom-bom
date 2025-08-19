package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record ArticleNewsletterStatisticsResponse(
        @Schema(required = true)
        int totalCount,

        @NotNull
        List<ArticleCountPerNewsletterResponse> newsletters
) {

    public static ArticleNewsletterStatisticsResponse of(
            int totalCount,
            List<ArticleCountPerNewsletterResponse> countResponse
    ) {
        return new ArticleNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
