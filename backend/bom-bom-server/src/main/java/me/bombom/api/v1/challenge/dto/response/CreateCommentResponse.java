package me.bombom.api.v1.challenge.dto.response;

public record CreateCommentResponse(

        boolean isFirstCompletion,
        boolean isChallengeCompleted
) {

    public static CreateCommentResponse of(boolean isFirstCompletion, boolean isChallengeCompleted) {
        return new CreateCommentResponse(isFirstCompletion, isChallengeCompleted);
    }
}
