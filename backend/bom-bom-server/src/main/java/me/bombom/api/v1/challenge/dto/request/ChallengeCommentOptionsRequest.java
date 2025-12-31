package me.bombom.api.v1.challenge.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record ChallengeCommentOptionsRequest(

        @NotNull
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate start,

        @NotNull
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate end
) {
}
