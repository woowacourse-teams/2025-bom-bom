package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;

public record StreakDayResponse(

        @Schema(requiredMode = RequiredMode.REQUIRED)
        LocalDate date,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        DayOfWeek dayOfWeek,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean shieldApplied
) {

    public static StreakDayResponse from(ChallengeDailyResult result) {
        return new StreakDayResponse(
                result.getDate(),
                result.getDate().getDayOfWeek(),
                result.getStatus() == ChallengeDailyStatus.SHIELD
        );
    }
}
