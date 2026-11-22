package me.bombom.api.v1.signupsource.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.dto.NewsletterResponse;
import me.bombom.api.v1.signupsource.domain.SignupRecommendation;

public record SignupRecommendationResponse(

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

        @Schema(description = "지금 구독 중인 회원 수", requiredMode = RequiredMode.REQUIRED)
        long subscriberCount,

        @Schema(description = "메일이 도착하는 요일 (예: 매주 금요일, 평일)")
        String arrivalDays
) {

    public static SignupRecommendationResponse of(
            NewsletterResponse newsletter,
            SignupRecommendation recommendation,
            long subscriberCount
    ) {
        return new SignupRecommendationResponse(
                newsletter.newsletterId(),
                newsletter.name(),
                newsletter.imageUrl(),
                newsletter.description(),
                newsletter.subscribeUrl(),
                newsletter.category(),
                subscriberCount,
                recommendation.getArrivalDays()
        );
    }
}
