package me.bombom.api.v1.challenge.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateCommentReplyRequest(

        @NotNull
        @Size(max = 500, message = "답글은 500자 이하로 작성 가능합니다.")
        String reply,

        boolean isPrivate
) {
}
