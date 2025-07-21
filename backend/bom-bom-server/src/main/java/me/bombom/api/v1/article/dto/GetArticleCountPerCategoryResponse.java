package me.bombom.api.v1.article.dto;

import me.bombom.api.v1.newsletter.domain.Category;

public record GetArticleCountPerCategoryResponse(
        String category,
        long count
) {

    public static GetArticleCountPerCategoryResponse of(Category category, long count) {
        return new GetArticleCountPerCategoryResponse(category.getName(), count);
    }
}
