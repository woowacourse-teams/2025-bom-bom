package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterBasicResponse;

public record ArticleDetailResponse(

        @NotNull
        String title,

        @NotNull
        String contents,

        @NotNull
        LocalDateTime arrivedDateTime,
        
        @Schema(required = true)
        int expectedReadTime,
        
        @Schema(required = true)
        boolean isRead,

        @NotNull
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
