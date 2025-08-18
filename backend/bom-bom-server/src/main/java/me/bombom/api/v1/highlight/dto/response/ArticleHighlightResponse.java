package me.bombom.api.v1.highlight.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.highlight.domain.Highlight;

public record ArticleHighlightResponse(
        @NotNull
        @Schema(type = "integer", format = "int64", description = "하이라이트 ID", required = true)
        Long id,

        @NotNull
        @Schema(type = "object", description = "하이라이트 위치 정보", required = true)
        HighlightLocationResponse location,

        @NotNull
        @Schema(type = "integer", format = "int64", description = "아티클 ID", required = true)
        Long articleId,

        @NotNull
        @Schema(type = "string", description = "하이라이트 색상", required = true)
        String color,

        @NotNull
        @Schema(type = "string", description = "하이라이트된 텍스트", required = true)
        String text,

        @Schema(type = "string", description = "메모")
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
