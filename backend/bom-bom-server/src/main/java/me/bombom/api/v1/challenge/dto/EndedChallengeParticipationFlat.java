package me.bombom.api.v1.challenge.dto;

public record EndedChallengeParticipationFlat(

        Long memberId,
        int attendedDays,
        int totalDays,
        boolean isSurvived
) {
}
