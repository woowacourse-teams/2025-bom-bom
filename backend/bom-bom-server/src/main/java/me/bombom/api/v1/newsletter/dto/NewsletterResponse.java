package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.NewsletterPublicationStatus;
import me.bombom.api.v1.newsletter.domain.NewsletterSource;

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
        Long categoryId,

        @NotNull
        String category,

        @NotNull
        NewsletterPublicationStatus status,

        @NotNull
        NewsletterSource source,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean isSubscribed
) {

    @QueryProjection
    public NewsletterResponse {
    }
}
