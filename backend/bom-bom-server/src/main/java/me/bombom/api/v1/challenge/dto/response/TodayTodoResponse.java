package me.bombom.api.v1.challenge.dto.response;

import me.bombom.api.v1.challenge.domain.ChallengeTodoStatus;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;

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
