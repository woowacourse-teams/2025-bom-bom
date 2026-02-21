package me.bombom.api.v1.subscribe.dto.response;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.NewsletterStatus;
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

        @NotNull
        String category,

        String unsubscribeUrl,

        @NotNull
        SubscribeStatus status,

        @NotNull
        NewsletterStatus newsletterStatus
) {

    public static SubscribedNewsletterResponse of(
            Long subscriptionId,
            Long newsletterId,
            String name,
            String imageUrl,
            String description,
            String category,
            String unsubscribeUrl,
            SubscribeStatus status,
            NewsletterStatus newsletterStatus
    ) {
        return new SubscribedNewsletterResponse(
                subscriptionId,
                newsletterId,
                name,
                imageUrl,
                description,
                category,
                unsubscribeUrl,
                status,
                newsletterStatus
        );
    }
}
