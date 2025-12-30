package me.bombom.api.v1.challenge.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChallengeCommentResponse(

        @NotNull
        String nickname,

        @NotNull
        String newsletterName,

        @Schema(required = true)
        boolean isSubscribed,

        @NotNull
        String articleTitle,

        String quotation,

        @NotNull
        String comment,

        @NotNull
        LocalDateTime createdAt
) {
}
