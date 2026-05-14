package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

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

        @Override
        public boolean equals(Object o) {
                if (this == o) return true;
                if (o == null || getClass() != o.getClass()) return false;
                PreviousArticleResponse that = (PreviousArticleResponse) o;
                return Objects.equals(articleId, that.articleId);
        }

        @Override
        public int hashCode() {
                return Objects.hash(articleId);
        }
}
