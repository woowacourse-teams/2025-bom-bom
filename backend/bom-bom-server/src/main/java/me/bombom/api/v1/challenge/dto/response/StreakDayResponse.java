package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;

public record StreakDayResponse(

        @Schema(requiredMode = RequiredMode.REQUIRED)
        LocalDate date,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        DayOfWeek dayOfWeek,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean isCompleted,

        @Schema(requiredMode = RequiredMode.REQUIRED)
        boolean isShieldApplied
) {

    public static StreakDayResponse from(ChallengeDailyResult result) {
        return new StreakDayResponse(
                result.getDate(),
                result.getDate().getDayOfWeek(),
                true,
                result.isShieldApplied()
        );
    }

    public static StreakDayResponse notParticipated(LocalDate date) {
        return new StreakDayResponse(date, date.getDayOfWeek(), false, false);
    }

    public static List<StreakDayResponse> from(List<ChallengeDailyResult> results) {
        return results.stream()
                .map(StreakDayResponse::from)
                .toList();
    }
}
