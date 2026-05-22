package me.bombom.openapi.common;

import java.util.List;
import me.bombom.openapi.model.NewsletterSummaryResponse;
import me.bombom.openapi.model.PageArticleResponse;
import org.springframework.data.domain.Page;

public final class ArticleResponseMapper {

    private ArticleResponseMapper() {
    }

    public static me.bombom.openapi.model.ArticleResponse toApi(
            me.bombom.api.v1.article.dto.response.ArticleResponse domain
    ) {
        NewsletterSummaryResponse newsletter = new NewsletterSummaryResponse(
                domain.newsletter().name(),
                domain.newsletter().imageUrl(),
                domain.newsletter().category()
        );

        return new me.bombom.openapi.model.ArticleResponse(
                domain.articleId(),
                domain.title(),
                domain.contentsSummary(),
                domain.arrivedDateTime(),
                domain.thumbnailUrl(),
                domain.expectedReadTime(),
                domain.isRead(),
                domain.isBookmarked(),
                newsletter
        );
    }

    public static PageArticleResponse toPage(Page<me.bombom.api.v1.article.dto.response.ArticleResponse> page) {
        List<me.bombom.openapi.model.ArticleResponse> content = page.getContent().stream()
                .map(ArticleResponseMapper::toApi)
                .toList();
        return new PageArticleResponse(
                content,
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber(),
                page.getSize(),
                page.getNumberOfElements(),
                page.isFirst(),
                page.isLast()
        );
    }
}
