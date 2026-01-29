package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record CommentReplyResponse(

        @NotNull
        Long replyId,

        String nickname,

        String profileImageUrl,

        @NotNull
        String reply,

        @NotNull
        LocalDateTime createdAt,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean isMyReply
) {
}
