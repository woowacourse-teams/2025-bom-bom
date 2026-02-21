package me.bombom.api.v1.newsletter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.NewsletterStatus;

public record NewsletterResponse(

        @NotNull
        Long newsletterId,

        @NotNull
        String name,

        String imageUrl,

        @NotNull
        String description,

        @NotNull
        String subscribeUrl,

        @NotNull
        String category,

        @NotNull
        NewsletterStatus status,

        @Schema(required = true)
        boolean isSubscribed
) {
}
