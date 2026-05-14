package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record WeeklyReadingResponse(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int readCount,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int goalCount
) {

    public static WeeklyReadingResponse from(WeeklyReading weeklyReading) {
        return new WeeklyReadingResponse(weeklyReading.getCurrentCount(), weeklyReading.getGoalCount());
    }
}
