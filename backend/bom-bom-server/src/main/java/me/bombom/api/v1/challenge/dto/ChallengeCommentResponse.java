package me.bombom.api.v1.challenge.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record ChallengeCommentResponse(

        String nickname,
        String newsletterName,
        String articleTitle,
        String quotation,
        String comment,
        LocalDateTime createdAt
) {
}
