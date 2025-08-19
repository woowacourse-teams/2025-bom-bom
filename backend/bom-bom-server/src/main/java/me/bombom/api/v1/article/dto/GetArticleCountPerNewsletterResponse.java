package me.bombom.api.v1.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record GetArticleCountPerNewsletterResponse(
        @Schema(required = true)
        long id,

        @NotNull
        String name,

        @NotNull
        String imageUrl,

        @Schema(required = true)
        long articleCount
) {

    public static GetArticleCountPerNewsletterResponse of(Newsletter newsletter, long count) {
        return new GetArticleCountPerNewsletterResponse(newsletter.getId(), newsletter.getName(), newsletter.getImageUrl(), count);
    }
}
