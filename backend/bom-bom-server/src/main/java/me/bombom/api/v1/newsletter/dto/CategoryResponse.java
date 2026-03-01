package me.bombom.api.v1.newsletter.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CategoryResponse(

        @NotNull
        Long id,

        @NotNull
        String name
) {

    public static List<CategoryResponse> from(List<NewsletterResponse> newsletters) {
        return newsletters.stream()
                .map(CategoryResponse::from)
                .distinct()
                .toList();
    }

    private static CategoryResponse from(NewsletterResponse newsletter) {
        return new CategoryResponse(newsletter.categoryId(), newsletter.category());
    }
}
