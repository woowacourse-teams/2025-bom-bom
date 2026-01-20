package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record ChallengeCommentResponse(

        @NotNull
        Long commentId,

        String nickname,

        String profileImageUrl,

        @NotNull
        String newsletterName,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean isSubscribed,

        @NotNull
        String articleTitle,

        String quotation,

        @NotNull
        String comment,

        @NotNull
        LocalDateTime createdAt,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean isMyComment
) {
}
