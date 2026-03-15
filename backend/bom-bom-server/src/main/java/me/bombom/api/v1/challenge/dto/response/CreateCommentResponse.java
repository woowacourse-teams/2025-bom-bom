package me.bombom.api.v1.challenge.dto.response;

public record CreateCommentResponse(

        boolean isFirstCompletion
) {

    public static CreateCommentResponse from(boolean isFirstCompletion) {
        return new CreateCommentResponse(isFirstCompletion);
    }
}
