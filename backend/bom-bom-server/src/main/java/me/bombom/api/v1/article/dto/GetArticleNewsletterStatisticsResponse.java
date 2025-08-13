package me.bombom.api.v1.article.dto;

import java.util.List;

public record GetArticleNewsletterStatisticsResponse(
        int totalCount,
        List<GetArticleCountPerNewsletterResponse> newsletters
) {

    public static GetArticleNewsletterStatisticsResponse of(
            int totalCount,
            List<GetArticleCountPerNewsletterResponse> countResponse
    ) {
        return new GetArticleNewsletterStatisticsResponse(totalCount, countResponse);
    }
}
