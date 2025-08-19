package me.bombom.api.v1.article.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.newsletter.dto.NewsletterSummaryResponse;

public record ArticleResponse(
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
        @Schema(type = "object", description = "뉴스레터 정보", required = true)
        NewsletterSummaryResponse newsletter
) {

    @QueryProjection
    public ArticleResponse {}
}
