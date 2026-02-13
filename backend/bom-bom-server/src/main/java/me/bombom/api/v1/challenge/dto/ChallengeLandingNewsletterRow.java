package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.challenge.dto.response.ChallengeLandingNewsletterResponse;

public record ChallengeLandingNewsletterRow(

        @NotNull
        Long challengeId,

        @NotNull
        Long newsletterId,

        @NotNull
        String newsletterName,

        @NotNull
        String newsletterImageUrl,

        @NotNull
        String category,

        @NotNull
        String description
) {

    public ChallengeLandingNewsletterResponse response() {
        return new ChallengeLandingNewsletterResponse(
                newsletterId,
                newsletterName,
                newsletterImageUrl,
                category,
                description
        );
    }
}

