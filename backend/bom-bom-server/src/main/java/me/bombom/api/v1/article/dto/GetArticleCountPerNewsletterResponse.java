package me.bombom.api.v1.article.dto;

import me.bombom.api.v1.newsletter.domain.Newsletter;

public record GetArticleCountPerNewsletterResponse(
        String newsletter,
        String imageUrl,
        long count
) {

    public static GetArticleCountPerNewsletterResponse of(Newsletter newsletter, long count) {
        return new GetArticleCountPerNewsletterResponse(newsletter.getName(), newsletter.getImageUrl(), count);
    }
}
