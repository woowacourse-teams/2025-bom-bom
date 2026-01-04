package me.bombom.api.v1.challenge.dto;

import me.bombom.api.v1.challenge.domain.ChallengeTodoType;

public record ChallengeProgressFlat(

        int totalDays,
        int completedDays,
        boolean isSurvived,
        ChallengeTodoType todoType,
        boolean isDone
) {
}
