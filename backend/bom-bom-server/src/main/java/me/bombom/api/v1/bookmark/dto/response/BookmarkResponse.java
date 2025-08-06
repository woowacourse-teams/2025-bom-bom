package me.bombom.api.v1.bookmark.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.time.LocalDateTime;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterSummaryResponse;

public record BookmarkResponse(
        Long id,
        Long articleId,
        String title,
        String contentsSummary,
        LocalDateTime arrivedDateTime,
        String thumbnailUrl,
        int expectedReadTime,
        boolean isRead,
        NewsletterSummaryResponse newsletter
) {
    @QueryProjection
    public BookmarkResponse(final Long id, final ArticleResponse articleResponse) {
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
