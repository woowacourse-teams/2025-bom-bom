package me.bombom.api.v1.blog.dto.response;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.blog.domain.BlogCategory;

public record BlogCategoryResponse(

        @NotNull
        Long id,

        @NotNull
        String categoryName
) {

    public static BlogCategoryResponse from(BlogCategory blogCategory) {
        return new BlogCategoryResponse(
                blogCategory.getId(),
                blogCategory.getName()
        );
    }

    public static List<BlogCategoryResponse> from(List<BlogCategory> blogCategory) {
        return blogCategory.stream()
                .map(BlogCategoryResponse::from)
                .toList();
    }
}
