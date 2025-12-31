package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.Challenge;

public record ChallengeSummaryResponse(

        @NotNull
        LocalDate startDate,

        @NotNull
        LocalDate endDate,

        @Schema(required = true)
        int totalDays
) {

    public static ChallengeSummaryResponse from(Challenge challenge) {
        return new ChallengeSummaryResponse(
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getTotalDays()
        );
    }
}
