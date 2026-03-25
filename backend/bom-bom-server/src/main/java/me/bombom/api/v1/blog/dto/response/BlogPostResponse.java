package me.bombom.api.v1.blog.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record BlogPostResponse(

        @NotNull
        Long postId,

        @NotNull
        String title,

        String thumbnailImageUrl,

        @NotNull
        String categoryName,

        @NotNull
        LocalDateTime publishedAt
) {
}
