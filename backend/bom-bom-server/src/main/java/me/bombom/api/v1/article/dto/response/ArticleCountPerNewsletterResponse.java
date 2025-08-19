package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record ArticleCountPerNewsletterResponse(
        @Schema(required = true)
        long id,

        @NotNull
        String name,

        @NotNull
        String imageUrl,

        @Schema(required = true)
        long articleCount
) {

    public static ArticleCountPerNewsletterResponse of(Newsletter newsletter, long count) {
        return new ArticleCountPerNewsletterResponse(newsletter.getId(), newsletter.getName(), newsletter.getImageUrl(), count);
    }
}
