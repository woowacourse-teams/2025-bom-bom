package me.bombom.api.v1.article.dto;

import java.time.LocalDateTime;
import me.bombom.api.v1.newsletter.dto.TodayArticleNewsletterResponse;

public record ArticleResponse(
        Long articleId,
        String title,
        String contentsSummary,
        LocalDateTime arrivedDateTime,
        String thumbnailUrl,
        int expectedReadTime,
        boolean isRead,
        TodayArticleNewsletterResponse newsletter
) {
}
