package me.bombom.api.v1.article.dto;

import java.util.List;

public record GetArticleCategoryStatisticsResponse(
        int totalCount,
        List<GetArticleCountPerCategoryResponse> categories
) {

    public static GetArticleCategoryStatisticsResponse of(
            int totalCount,
            List<GetArticleCountPerCategoryResponse> countResponse
    ) {
        return new GetArticleCategoryStatisticsResponse(totalCount, countResponse);
    }
}
