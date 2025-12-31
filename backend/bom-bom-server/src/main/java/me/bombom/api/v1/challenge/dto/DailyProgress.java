package me.bombom.api.v1.challenge.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;

public record DailyProgress(

        @NotNull
        LocalDate date,

        @NotNull
        ChallengeDailyStatus status
)
{
}
