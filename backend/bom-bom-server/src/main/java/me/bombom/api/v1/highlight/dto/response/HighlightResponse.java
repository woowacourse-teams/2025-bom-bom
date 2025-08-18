package me.bombom.api.v1.highlight.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record HighlightResponse(
        @NotNull
        Long id,

        @NotNull
        HighlightLocationResponse location,

        @NotNull
        Long articleId,

        @NotNull
        String color,

        @NotNull
        String text,
        
        String memo,

        String newsletterName,

        String newsletterImageUrl,

        String ariticleTitle,

        LocalDateTime createdAt
) {

    @QueryProjection
    public HighlightResponse {
    }

    public static HighlightResponse of(Highlight highlight, Article article, Newsletter newsletter){
        return new HighlightResponse(
                highlight.getId(),
                HighlightLocationResponse.from(highlight.getHighlightLocation()),
                highlight.getArticleId(),
                highlight.getColor().getValue(),
                highlight.getText(),
                highlight.getMemo(),
                newsletter.getName(),
                newsletter.getImageUrl(),
                article.getTitle(),
                highlight.getCreatedAt()
        );
    }
}
