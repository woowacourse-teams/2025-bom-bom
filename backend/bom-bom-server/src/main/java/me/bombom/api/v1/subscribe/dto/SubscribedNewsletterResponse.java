package me.bombom.api.v1.subscribe.dto;

import jakarta.validation.constraints.NotNull;
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
        SubscribeStatus status
) {

    public static SubscribedNewsletterResponse of(
            Long subscriptionId,
            Long newsletterId,
            String name,
            String imageUrl,
            String description,
            String category,
            String unsubscribeUrl,
            SubscribeStatus status
    ) {
        return new SubscribedNewsletterResponse(
                subscriptionId,
                newsletterId,
                name,
                imageUrl,
                description,
                category,
                unsubscribeUrl,
                status
        );
    }
}
