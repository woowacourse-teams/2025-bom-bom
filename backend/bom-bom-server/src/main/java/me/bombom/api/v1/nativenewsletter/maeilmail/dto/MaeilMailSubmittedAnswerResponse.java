package me.bombom.api.v1.nativenewsletter.maeilmail.dto;

import jakarta.validation.constraints.NotNull;

public record MaeilMailSubmittedAnswerResponse(

        @NotNull
        String answer
) {
}
