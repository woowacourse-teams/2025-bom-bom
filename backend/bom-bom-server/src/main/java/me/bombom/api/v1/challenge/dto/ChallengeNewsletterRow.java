package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.challenge.dto.response.ChallengeNewsletterResponse;

public record ChallengeNewsletterRow(

        @NotNull
        Long challengeId,

        @NotNull
        Long newsletterId,

        @NotNull
        String newsletterName,

        @NotNull
        String newsletterImageUrl
) {

    public ChallengeNewsletterResponse response() {
        return new ChallengeNewsletterResponse(newsletterId, newsletterName, newsletterImageUrl);
    }
}
