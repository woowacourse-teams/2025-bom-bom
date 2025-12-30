package me.bombom.api.v1.challenge.dto.response;

import java.time.LocalDate;

public record ChallengeSummaryResponse(

        LocalDate startDate,
        LocalDate endDate,
        int totalDays
) {
}
