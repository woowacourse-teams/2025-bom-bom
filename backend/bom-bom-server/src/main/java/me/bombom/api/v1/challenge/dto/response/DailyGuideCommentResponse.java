package me.bombom.api.v1.challenge.dto.response;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record DailyGuideCommentResponse(

        @NotNull
        String nickname,

        @NotNull
        String comment,

        @NotNull
        LocalDateTime createdAt
) {

    public static DailyGuideCommentResponse of(String nickname, String comment, LocalDateTime createdAt) {
        return new DailyGuideCommentResponse(nickname, comment, createdAt);
    }
}
