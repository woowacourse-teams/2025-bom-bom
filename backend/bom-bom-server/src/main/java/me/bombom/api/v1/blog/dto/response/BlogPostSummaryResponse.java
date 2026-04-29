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

    public static BlogPostSummaryResponse of(BlogPostSummaryRow summaryRow, List<String> hashtags) {
        return new BlogPostSummaryResponse(
                summaryRow.postId(),
                summaryRow.title(),
                summaryRow.description(),
                summaryRow.thumbnailImageUrl(),
                summaryRow.categoryName(),
                summaryRow.publishedAt(),
                hashtags
        );
    }
}
