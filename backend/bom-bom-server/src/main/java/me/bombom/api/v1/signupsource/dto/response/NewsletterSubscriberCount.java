package me.bombom.api.v1.signupsource.dto.response;

public record NewsletterSubscriberCount(
        Long newsletterId,
        long subscriberCount
) {
}
