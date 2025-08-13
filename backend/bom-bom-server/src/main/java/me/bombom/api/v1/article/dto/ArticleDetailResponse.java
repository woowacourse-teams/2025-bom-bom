package me.bombom.api.v1.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterBasicResponse;

public record ArticleDetailResponse(
        @NotNull
        @Schema(type = "string", description = "아티클 제목", required = true)
        String title,

        @NotNull
        @Schema(type = "string", description = "아티클 내용", required = true)
        String contents,

        @NotNull
        @Schema(type = "string", format = "date-time", description = "도착 시간", required = true)
        LocalDateTime arrivedDateTime,
        
        @Schema(type = "integer", format = "int32", description = "예상 읽기 시간(분)", required = true)
        int expectedReadTime,
        
        @Schema(type = "boolean", description = "읽음 여부", required = true)
        boolean isRead,

        @NotNull
        @Schema(type = "object", description = "뉴스레터 정보", required = true)
        NewsletterBasicResponse newsletter
) {

    public static ArticleDetailResponse of(Article article, Newsletter newsletter, Category category) {
        return new ArticleDetailResponse(
                article.getTitle(),
                article.getContents(),
                article.getArrivedDateTime(),
                article.getExpectedReadTime(),
                article.isRead(),
                NewsletterBasicResponse.of(newsletter, category)
        );
    }
}
