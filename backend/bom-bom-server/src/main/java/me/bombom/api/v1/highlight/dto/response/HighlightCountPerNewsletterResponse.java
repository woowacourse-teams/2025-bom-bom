package me.bombom.api.v1.highlight.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record HighlightCountPerNewsletterResponse(
        @Schema(type = "integer", format = "int64", description = "뉴스레터 id", required = true)
        long id,

        @NotNull
        String name,

        @NotNull
        String imageUrl,

        @Schema(type = "integer", format = "int64", description = "아티클 수", required = true)
        long highlightCount
) {

    @QueryProjection
    public HighlightCountPerNewsletterResponse {}

    public static HighlightCountPerNewsletterResponse of(Newsletter newsletter, long highlightCount) {
        return new HighlightCountPerNewsletterResponse(newsletter.getId(), newsletter.getName(), newsletter.getImageUrl(), highlightCount);
    }
}
