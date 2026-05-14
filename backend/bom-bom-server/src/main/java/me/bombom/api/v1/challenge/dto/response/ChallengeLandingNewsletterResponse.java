package me.bombom.api.v1.challenge.dto.response;

import jakarta.validation.constraints.NotNull;

public record ChallengeLandingNewsletterResponse(

        @NotNull
        Long id,

        @NotNull
        String name,

        @NotNull
        String imageUrl,

        @NotNull
        String category,

        @NotNull
        String description
) {
}

