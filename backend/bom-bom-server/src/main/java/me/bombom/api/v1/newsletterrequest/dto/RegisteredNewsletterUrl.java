package me.bombom.api.v1.newsletterrequest.dto;

public record RegisteredNewsletterUrl(
        Long newsletterId,
        String mainPageUrl,
        String subscribeUrl
) {
}
