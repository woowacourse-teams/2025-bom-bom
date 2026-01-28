package me.bombom.api.v1.subscribe.dto;

import jakarta.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

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

        @NotNull
        boolean hasUnsubscribeUrl
) {

    public static SubscribedNewsletterResponse of(
            Long subscriptionId,
            Long newsletterId,
            String name,
            String imageUrl,
            String description,
            String category,
            String unsubscribeUrl
    ) {
        boolean hasUnsubscribeUrl = StringUtils.hasText(unsubscribeUrl);
        return new SubscribedNewsletterResponse(
                subscriptionId, newsletterId, name, imageUrl, description, category, hasUnsubscribeUrl
        );
    }
}
