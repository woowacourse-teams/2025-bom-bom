package me.bombom.api.v1.challenge.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ChallengeCommentRequest(

        @NotNull
        Long articleId,

        String quotation,

        @NotNull
        @Size(min = 20, message = "댓글은 20자 이상 입력해야 합니다.")
        String comment
) {
}
