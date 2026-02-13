package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.domain.Challenge;

public record ChallengeLandingResponse(

        @NotNull
        String name,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        int generation,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean grantsBadge,

        @NotNull
        List<ChallengeLandingNewsletterResponse> newsletters
) {

    public static ChallengeLandingResponse of(
            Challenge challenge,
            List<ChallengeLandingNewsletterResponse> newsletters,
            boolean grantsBadge
    ) {
        return new ChallengeLandingResponse(
                challenge.getName(),
                challenge.getGeneration(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                grantsBadge,
                newsletters
        );
    }
}

