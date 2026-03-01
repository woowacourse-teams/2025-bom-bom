package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public record CategoryResponse(

        @NotNull
        Long id,

        @NotNull
        String name
) {

    @QueryProjection
    public CategoryResponse {
    }

    public static List<CategoryResponse> from(List<NewsletterResponse> newsletters) {
        return newsletters.stream()
                .map(n -> new CategoryResponse(n.categoryId(), n.category()))
                .distinct()
                .toList();
    }
}
