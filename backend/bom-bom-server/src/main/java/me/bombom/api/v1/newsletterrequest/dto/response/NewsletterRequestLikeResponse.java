package me.bombom.api.v1.newsletterrequest.dto.response;

import me.bombom.api.v1.newsletterrequest.domain.NewsletterRequest;

public record NewsletterRequestLikeResponse(

        int likeCount
) {

    public static NewsletterRequestLikeResponse from(NewsletterRequest newsletterRequest) {
        return new NewsletterRequestLikeResponse(newsletterRequest.getLikeCount());
    }
}
