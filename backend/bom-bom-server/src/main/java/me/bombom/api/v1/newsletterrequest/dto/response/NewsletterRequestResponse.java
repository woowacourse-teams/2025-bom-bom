package me.bombom.api.v1.newsletterrequest.dto.response;

import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequestStatus;
import me.bombom.api.v1.newsletterrequest.dto.NewsletterRequestRow;

public record NewsletterRequestResponse(
        Long id,
        String name,
        String url,
        NewsletterRequestStatus status,
        int likeCount,
        boolean liked,
        boolean mine,
        String categoryName,
        String imageUrl,
        Long newsletterId
) {

    public static NewsletterRequestResponse of(
            NewsletterRequestRow row,
            boolean liked,
            Long memberId
    ) {
        return new NewsletterRequestResponse(
                row.id(),
                row.draftName() != null ? row.draftName() : row.requestedName(),
                row.requestedUrl(),
                row.status(),
                row.likeCount(),
                liked,
                row.requesterMemberId().equals(memberId),
                row.categoryName(),
                row.imageUrl(),
                row.newsletterId()
        );
    }
}
