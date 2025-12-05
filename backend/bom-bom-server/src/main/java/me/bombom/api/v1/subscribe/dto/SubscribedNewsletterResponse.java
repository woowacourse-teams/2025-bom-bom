package me.bombom.api.v1.subscribe.dto;

import jakarta.validation.constraints.NotNull;

import org.springframework.util.StringUtils;

public record SubscribedNewsletterResponse(

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
    public SubscribedNewsletterResponse(Long newsletterId, String name, String imageUrl, String description, String category, String unsubscribeUrl) {
        this(newsletterId, name, imageUrl, description, category, StringUtils.hasText(unsubscribeUrl));
    }
}
