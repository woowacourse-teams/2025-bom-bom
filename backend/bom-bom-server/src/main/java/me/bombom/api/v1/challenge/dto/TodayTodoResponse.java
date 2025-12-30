package me.bombom.api.v1.challenge.dto;

import me.bombom.api.v1.challenge.domain.ChallengeTodoStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;

public record TodayTodoResponse(

        ChallengeTodoType challengeTodoType,
        ChallengeTodoStatus challengeTodoStatus
) {

    public static TodayTodoResponse from(ChallengeProgressFlat progress) {
        return new TodayTodoResponse(
                progress.todoType(),
                progress.isDone() ? ChallengeTodoStatus.COMPLETE : ChallengeTodoStatus.INCOMPLETE
        );
    }
}
