package me.bombom.api.v1.newsletterrequest.dto.response;

public record NewsletterSuggestionResponse(
        Long newsletterId,
        String name,
        String imageUrl
) {
}
