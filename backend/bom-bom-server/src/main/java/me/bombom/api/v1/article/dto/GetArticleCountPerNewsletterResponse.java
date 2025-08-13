package me.bombom.api.v1.article.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record GetArticleCountPerNewsletterResponse(
        @NotNull
        @Schema(type = "string", description = "뉴스레터명", required = true)
        String newsletter,

        @NotNull
        @Schema(type = "string", description = "이미지 url", required = true)
        String imageUrl,

        @Schema(type = "integer", format = "int64", description = "아티클 수", required = true)
        long count
) {

    public static GetArticleCountPerNewsletterResponse of(Newsletter newsletter, long count) {
        return new GetArticleCountPerNewsletterResponse(newsletter.getName(), newsletter.getImageUrl(), count);
    }
}
