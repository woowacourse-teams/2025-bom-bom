package me.bombom.api.v1.blog.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
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

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int expectedReadTime,

        @NotNull
        LocalDateTime publishedAt
) {
}
