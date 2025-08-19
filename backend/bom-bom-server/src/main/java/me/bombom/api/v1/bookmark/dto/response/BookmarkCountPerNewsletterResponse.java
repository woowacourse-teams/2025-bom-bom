package me.bombom.api.v1.bookmark.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record BookmarkCountPerNewsletterResponse(
        Long id,

        @NotNull
        String name,

        @NotNull
        String imageUrl,

        @Schema(required = true)
        int bookmarkCount
) {

    public static BookmarkCountPerNewsletterResponse of(Newsletter newsletter, int count) {
        return new BookmarkCountPerNewsletterResponse(newsletter.getId(), newsletter.getName(), newsletter.getImageUrl(), count);
    }
}
