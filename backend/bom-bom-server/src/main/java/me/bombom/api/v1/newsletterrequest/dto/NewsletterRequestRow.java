package me.bombom.api.v1.newsletterrequest.dto;

import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;

public record NewsletterRequestRow(
        Long id,
        String requestedName,
        String draftName,
        String requestedUrl,
        NewsletterRequestStatus status,
        int likeCount,
        Long requesterMemberId,
        String categoryName,
        String imageUrl,
        Long newsletterId
) {
}
