package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record WeeklyReadingResponse(

        @Schema(
                type = "integer",
                format = "int32",
                description = "읽은 아티클 수",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int readCount,

        @Schema(
                type = "integer",
                format = "int32",
                description = "목표 읽기 수",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int goalCount
) {

    public static WeeklyReadingResponse from(WeeklyReading weeklyReading) {
        return new WeeklyReadingResponse(weeklyReading.getCurrentCount(), weeklyReading.getGoalCount());
    }
}
