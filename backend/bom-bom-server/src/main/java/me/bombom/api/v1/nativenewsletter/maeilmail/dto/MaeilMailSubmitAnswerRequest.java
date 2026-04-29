package me.bombom.api.v1.nativenewsletter.maeilmail.dto;


import jakarta.validation.constraints.NotBlank;

public record MaeilMailSubmitAnswerRequest(

        @NotBlank(message = "답변은 필수로 입력해야 합니다.")
        String answer
) {
}
