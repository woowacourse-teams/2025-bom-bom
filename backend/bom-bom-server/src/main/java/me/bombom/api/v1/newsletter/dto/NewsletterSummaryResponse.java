package me.bombom.api.v1.newsletter.dto;

public record NewsletterSummaryResponse(
        String name,
        String imageUrl,
        String category
) {
}
