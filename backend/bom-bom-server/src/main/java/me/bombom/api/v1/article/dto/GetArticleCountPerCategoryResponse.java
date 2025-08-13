package me.bombom.api.v1.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Category;

public record GetArticleCountPerCategoryResponse(
        @NotNull
        @Schema(type = "string", description = "카테고리명", required = true)
        String category,
        
        @Schema(type = "integer", format = "int64", description = "아티클 수", required = true)
        long count
) {

    public static GetArticleCountPerCategoryResponse of(Category category, long count) {
        return new GetArticleCountPerCategoryResponse(category.getName(), count);
    }
}
