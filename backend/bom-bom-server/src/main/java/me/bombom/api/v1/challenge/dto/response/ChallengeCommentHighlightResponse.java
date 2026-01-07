package me.bombom.api.v1.challenge.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import jakarta.validation.constraints.NotNull;

public record ChallengeCommentHighlightResponse(

        @NotNull
        Long highlightId,

        @NotNull
        String text,

        String memo
) {

    @QueryProjection
    public ChallengeCommentHighlightResponse {
    }
}
