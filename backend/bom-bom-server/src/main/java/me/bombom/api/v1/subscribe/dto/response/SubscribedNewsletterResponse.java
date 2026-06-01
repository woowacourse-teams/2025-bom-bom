package me.bombom.api.v1.subscribe.dto.response;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;
import me.bombom.api.v1.subscribe.domain.SubscribeStatus;

public record SubscribedNewsletterResponse(

        @NotNull
        Long subscriptionId,

        @NotNull
        Long newsletterId,

        @NotNull
        String name,

        String imageUrl,

        @NotNull
        String description,

        String unsubscribeUrl,

        @NotNull
        SubscribeStatus status,

        @NotNull
        NewsletterPublicationStatus newsletterPublicationStatus,

        @NotNull
        NewsletterSource newsletterSource
) {

    public static SubscribedNewsletterResponse of(
            Long subscriptionId,
            Long newsletterId,
            String name,
            String imageUrl,
            String description,
            String unsubscribeUrl,
            SubscribeStatus status,
            NewsletterPublicationStatus publicationStatus,
            NewsletterSource newsletterSource
    ) {
        return new SubscribedNewsletterResponse(
                subscriptionId,
                newsletterId,
                name,
                imageUrl,
                description,
                unsubscribeUrl,
                status,
                publicationStatus,
                newsletterSource
        );
    }
}
