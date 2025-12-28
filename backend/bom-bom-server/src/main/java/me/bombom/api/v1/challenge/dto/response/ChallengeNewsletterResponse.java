package me.bombom.api.v1.challenge.dto.response;

import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record ChallengeNewsletterResponse(

        @NotNull
        Long id,

        @NotNull
        String name,

        @NotNull
        String imageUrl
) {
    public static ChallengeNewsletterResponse from(Newsletter newsletter) {
        return new ChallengeNewsletterResponse(
                newsletter.getId(),
                newsletter.getName(),
                newsletter.getImageUrl()
        );
    }
}
