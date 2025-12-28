package me.bombom.api.v1.challenge.dto;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.Challenge;

public record GetChallengeInfoResponse(

        String name,
        LocalDate startDate,
        LocalDate endDate,
        int generation,
        int totalDays,
        int requiredDays
) {

    public static GetChallengeInfoResponse of(Challenge challenge, int successRequiredRatio) {
        return new GetChallengeInfoResponse(
                challenge.getName(),
                challenge.getStartDate(),
                challenge.getEndDate(),
                challenge.getGeneration(),
                challenge.getTotalDays(),
                challenge.getTotalDays() * successRequiredRatio
        );
    }
}
