package me.bombom.api.v1.nativenewsletter.maeilmail.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MaeilMailSubmitAnswerRequest(

        @Schema(description = "사용자가 제출한 매일메일 답변", maxLength = 16000)
        @NotBlank(message = "답변은 필수로 입력해야 합니다.")
        @Size(max = 16000, message = "답변은 16,000자 이하로 입력해야 합니다.")
        String answer
) {
}
