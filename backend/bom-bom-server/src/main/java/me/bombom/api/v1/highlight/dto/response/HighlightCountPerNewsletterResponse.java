package me.bombom.api.v1.highlight.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record HighlightCountPerNewsletterResponse(
        @NotNull
        @Schema(type = "integer", format = "int64", description = "뉴스레터 id", required = true)
        long id,

        @NotNull
        @Schema(type = "string", description = "뉴스레터명", required = true)
        String name,

        @NotNull
        @Schema(type = "string", description = "이미지 url", required = true)
        String imageUrl,

        @Schema(type = "integer", format = "int64", description = "아티클 수", required = true)
        long highlightCount
) {

    public static HighlightCountPerNewsletterResponse of(Newsletter newsletter, long highlightCount) {
        return new HighlightCountPerNewsletterResponse(newsletter.getId(), newsletter.getName(), newsletter.getImageUrl(), highlightCount);
    }
}
