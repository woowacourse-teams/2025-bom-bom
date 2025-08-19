package me.bombom.api.v1.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record GetBookmarkCountPerNewsletterResponse(
        @Schema(type = "integer", format = "int64", description = "뉴스레터 id", required = true)
        long id,

        @NotNull
        @Schema(type = "string", description = "뉴스레터명", required = true)
        String name,

        @NotNull
        @Schema(type = "string", description = "이미지 url", required = true)
        String imageUrl,

        @Schema(type = "integer", format = "int64", description = "아티클 수", required = true)
        long bookmarkCount
) {

    public static GetBookmarkCountPerNewsletterResponse of(Newsletter newsletter, long count) {
        return new GetBookmarkCountPerNewsletterResponse(newsletter.getId(), newsletter.getName(), newsletter.getImageUrl(), count);
    }
}
