package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.NotBlank;

public record MaeilMailIdealAnswerResponse(

        @NotBlank
        String title,

        @NotBlank
        String answer
) {
}
