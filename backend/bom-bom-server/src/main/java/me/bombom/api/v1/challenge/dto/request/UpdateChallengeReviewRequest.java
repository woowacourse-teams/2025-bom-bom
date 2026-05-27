package me.bombom.api.v1.challenge.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateChallengeReviewRequest(

        @NotBlank(message = "리뷰 내용은 필수 항목입니다.")
        @Size(max = 500, message = "리뷰 내용은 최대 500자까지 입력할 수 있습니다.")
        String comment,

        boolean isPrivate
) {
}
