package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.Challenge;

public record ChallengeInfoResponse(

        @NotNull
        String name,

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Schema(required = true)
        int generation,

        @Schema(required = true)
        int totalDays,

        @Schema(required = true)
        int requiredDays
) {

    public static ChallengeInfoResponse of(Challenge challenge, double successRequiredRatio) {
        return new ChallengeInfoResponse(
                challenge.getName(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getGeneration(),
                challenge.getTotalDays(),
                (int) Math.ceil(challenge.getTotalDays() * successRequiredRatio)
        );
    }
}
