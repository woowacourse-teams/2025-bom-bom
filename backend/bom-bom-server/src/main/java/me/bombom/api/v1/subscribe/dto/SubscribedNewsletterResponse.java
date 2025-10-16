package me.bombom.api.v1.subscribe.dto;

import jakarta.validation.constraints.NotNull;

public record SubscribedNewsletterResponse(

        @NotNull
        Long newsletterId,

        @NotNull
        String name,

        String imageUrl,

        @NotNull
        String description,

        @NotNull
        String category
) {
}
