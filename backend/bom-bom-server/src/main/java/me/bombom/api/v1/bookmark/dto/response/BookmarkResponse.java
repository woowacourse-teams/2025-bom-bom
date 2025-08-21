package me.bombom.api.v1.bookmark.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.article.dto.response.ArticleResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterSummaryResponse;

public record BookmarkResponse(

        @NotNull
        Long id,

        @NotNull
        Long articleId,

        @NotNull
        String title,

        @NotNull
        String contentsSummary,

        @NotNull
        LocalDateTime arrivedDateTime,

        String thumbnailUrl,

        @Schema(required = true)
        int expectedReadTime,
        
        @Schema(required = true)
        boolean isRead,

        @NotNull
        NewsletterSummaryResponse newsletter
) {

    @QueryProjection
    public BookmarkResponse(Long id, ArticleResponse articleResponse) {
        this(
                id,
                articleResponse.articleId(),
                articleResponse.title(),
                articleResponse.contentsSummary(),
                articleResponse.arrivedDateTime(),
                articleResponse.thumbnailUrl(),
                articleResponse.expectedReadTime(),
                articleResponse.isRead(),
                articleResponse.newsletter()
        );
    }
}
