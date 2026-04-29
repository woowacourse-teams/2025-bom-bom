package me.bombom.api.v1.blog.dto.response;

import java.time.LocalDateTime;

public record BlogPostSummaryRow(
        Long postId,
        String title,
        String description,
        String thumbnailImageUrl,
        String categoryName,
        LocalDateTime publishedAt
) {
}
