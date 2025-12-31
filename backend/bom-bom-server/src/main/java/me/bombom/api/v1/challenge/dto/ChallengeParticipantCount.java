package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.NotNull;

public record ChallengeParticipantCount(

        @NotNull
        Long challengeId,

        @NotNull
        Long count
) {
}
