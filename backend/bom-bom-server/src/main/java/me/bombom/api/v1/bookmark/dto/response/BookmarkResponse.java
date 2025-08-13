package me.bombom.api.v1.bookmark.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.article.dto.ArticleResponse;
import me.bombom.api.v1.newsletter.dto.NewsletterSummaryResponse;

public record BookmarkResponse(
        @NotNull
        @Schema(type = "integer", format = "int64", description = "북마크 ID", required = true)
        Long id,

        @NotNull
        @Schema(type = "integer", format = "int64", description = "아티클 ID", required = true)
        Long articleId,

        @NotNull
        @Schema(type = "string", description = "아티클 제목", required = true)
        String title,

        @NotNull
        @Schema(type = "string", description = "아티클 내용 요약", required = true)
        String contentsSummary,

        @NotNull
        @Schema(type = "string", format = "date-time", description = "도착 시간", required = true)
        LocalDateTime arrivedDateTime,

        @Schema(type = "string", description = "썸네일 URL")
        String thumbnailUrl,

        @Schema(type = "integer", format = "int32", description = "예상 읽기 시간(분)", required = true)
        int expectedReadTime,
        
        @Schema(type = "boolean", description = "읽음 여부", required = true)
        boolean isRead,

        @NotNull
        @Schema(type = "object", description = "뉴스레터 정보", required = true)
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
