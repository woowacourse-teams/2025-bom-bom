package me.bombom.api.v1.challenge.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateChallengeReviewRequest(

        @NotBlank(message = "리뷰 내용은 필수 항목입니다.")
        String comment,

        boolean isPrivate
) {
}
