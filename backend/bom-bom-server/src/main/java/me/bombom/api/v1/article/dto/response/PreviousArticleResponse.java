package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.PreviousArticleSource;

public record PreviousArticleResponse(

        @NotNull
        Long id,

        @NotNull
        PreviousArticleSource source,

        @NotNull
        String title,

        @NotNull
        String contentsSummary,

        @Schema(required = true)
        int expectedReadTime
) {
}
