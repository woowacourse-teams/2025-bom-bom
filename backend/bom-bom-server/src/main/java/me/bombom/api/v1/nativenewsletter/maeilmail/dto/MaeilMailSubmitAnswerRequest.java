package me.bombom.api.v1.nativenewsletter.maeilmail.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record MaeilMailSubmitAnswerRequest(

        @Positive(message = "id는 1 이상의 값이어야 합니다.")
        Long contentId,

        @NotBlank(message = "답변은 필수로 입력해야 합니다.")
        String answer
) {
}
