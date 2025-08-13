package me.bombom.api.v1.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record GetArticleCategoryStatisticsResponse(
        @Schema(type = "integer", format = "int32", description = "전체 아티클 수", required = true)
        int totalCount,

        @NotNull
        @Schema(type = "array", description = "카테고리별 통계", required = true)
        List<GetArticleCountPerCategoryResponse> categories
) {

    public static GetArticleCategoryStatisticsResponse of(
            int totalCount,
            List<GetArticleCountPerCategoryResponse> countResponse
    ) {
        return new GetArticleCategoryStatisticsResponse(totalCount, countResponse);
    }
}
