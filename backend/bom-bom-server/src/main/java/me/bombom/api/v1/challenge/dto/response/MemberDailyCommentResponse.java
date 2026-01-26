package me.bombom.api.v1.challenge.dto.response;

import jakarta.validation.constraints.NotNull;

public record MemberDailyCommentResponse(

        @NotNull
        String comment
) {
}
