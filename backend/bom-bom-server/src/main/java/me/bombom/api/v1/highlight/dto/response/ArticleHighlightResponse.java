package me.bombom.api.v1.highlight.dto.response;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.highlight.domain.Highlight;

public record ArticleHighlightResponse(

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

        String memo
    ) {

    public static ArticleHighlightResponse from(Highlight highlight) {
        return new ArticleHighlightResponse(
                highlight.getId(),
                HighlightLocationResponse.from(highlight.getHighlightLocation()),
                highlight.getArticleId(),
                highlight.getColor().getValue(),
                highlight.getText(),
                highlight.getMemo()
        );
    }
}
