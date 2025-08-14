package me.bombom.api.v1.bookmark.dto.response;

import me.bombom.api.v1.newsletter.domain.Newsletter;

public record GetBookmarkCountPerNewsletterResponse(
        String newsletter,
        String imageUrl,
        long count
) {

    public static GetBookmarkCountPerNewsletterResponse of(Newsletter newsletter, long count) {
        return new GetBookmarkCountPerNewsletterResponse(newsletter.getName(), newsletter.getImageUrl(), count);
    }
}
