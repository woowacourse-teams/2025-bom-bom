package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record WeeklyGoalCountResponse(

        @NotNull
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        Long weeklyReadingId,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int weeklyGoalCount
) {

    public static WeeklyGoalCountResponse from(WeeklyReading weeklyReading) {
        return new WeeklyGoalCountResponse(weeklyReading.getId(), weeklyReading.getGoalCount());
    }
}
