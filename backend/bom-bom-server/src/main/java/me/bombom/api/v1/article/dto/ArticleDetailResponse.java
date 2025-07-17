package me.bombom.api.v1.article.dto;

import java.time.LocalDateTime;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;
import me.bombom.api.v1.newsletter.dto.NewsletterBasicResponse;

public record ArticleDetailResponse(
        String title,
        String contents,
        LocalDateTime arrivedDateTime,
        int expectedReadTime,
        NewsletterBasicResponse newsletter
) {

    public static ArticleDetailResponse of(Article article, Newsletter newsletter, Category category) {
        return new ArticleDetailResponse(
                article.getTitle(),
                article.getContents(),
                article.getArrivedDateTime(),
                article.getExpectedReadTime(),
                NewsletterBasicResponse.of(newsletter, category)
        );
    }
}
