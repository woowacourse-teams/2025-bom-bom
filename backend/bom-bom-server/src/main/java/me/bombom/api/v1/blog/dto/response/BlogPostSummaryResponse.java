package me.bombom.api.v1.blog.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

public record BlogPostSummaryResponse(

        @NotNull
        Long postId,

        @NotNull
        String title,

        @NotNull
        String description,

        String thumbnailImageUrl,

        String categoryName,

        @NotNull
        LocalDateTime publishedAt,

        @NotNull
        List<String> hashtags
) {

    public BlogPostSummaryResponse(
            Long postId,
            String title,
            String description,
            String thumbnailImageUrl,
            String categoryName,
            LocalDateTime publishedAt
    ) {
        this(postId, title, description, thumbnailImageUrl, categoryName, publishedAt, List.of());
    }

    public BlogPostSummaryResponse withHashtags(List<String> hashtags) {
        return new BlogPostSummaryResponse(
                postId,
                title,
                description,
                thumbnailImageUrl,
                categoryName,
                publishedAt,
                List.copyOf(hashtags)
        );
    }
}
