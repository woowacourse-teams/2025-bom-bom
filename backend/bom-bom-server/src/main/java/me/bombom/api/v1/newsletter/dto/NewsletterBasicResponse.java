package me.bombom.api.v1.newsletter.dto;

import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record NewsletterBasicResponse(
        String name,
        String email,
        String imageUrl,
        String category
) {

    public static NewsletterBasicResponse of(Newsletter newsletter, Category category) {
        return new NewsletterBasicResponse(
                newsletter.getName(),
                newsletter.getEmail(),
                newsletter.getImageUrl(),
                category.getName()
        );
    }
}
