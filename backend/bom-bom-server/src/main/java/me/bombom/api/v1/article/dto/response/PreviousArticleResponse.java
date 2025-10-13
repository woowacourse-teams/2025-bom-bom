package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record PreviousArticleResponse(

        @NotNull
        Long articleId,

        @NotNull
        String title,

        @NotNull
        String contentsSummary,

        @Schema(required = true)
        int expectedReadTime
) {
}
