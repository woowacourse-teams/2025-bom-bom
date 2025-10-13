package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterBasicResponse;

public record PreviousArticleDetailResponse(

        @NotNull
        String title,

        @NotNull
        String contents,

        @NotNull
        LocalDateTime arrivedDateTime,
        
        @Schema(required = true)
        int expectedReadTime,

        @NotNull
        NewsletterBasicResponse newsletter
) {

    public static PreviousArticleDetailResponse of(Article article, Newsletter newsletter, Category category) {
        return new PreviousArticleDetailResponse(
                article.getTitle(),
                article.getContents(),
                article.getArrivedDateTime(),
                article.getExpectedReadTime(),
                NewsletterBasicResponse.of(newsletter, category)
        );
    }
}
