package me.bombom.api.v1.newsletter.dto;

public record NewsletterResponse(
        Long newsletterId,
        String name,
        String imageUrl,
        String description,
        String mainPageUrl
) {
}
