package me.bombom.api.v1.challenge.dto.response;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.Challenge;

public record ChallengeSummaryResponse(

        LocalDate startDate,
        LocalDate endDate,
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
