package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record WeeklyGoalCountResponse(

        @NotNull
        @Schema(type = "integer", format = "int64", description = "주간 읽기 ID", required = true)
        Long weeklyReadingId,
        
        @Schema(type = "integer", format = "int32", description = "주간 목표 읽기 수", required = true)
        int weeklyGoalCount
) {

    public static WeeklyGoalCountResponse from(WeeklyReading weeklyReading) {
        return new WeeklyGoalCountResponse(weeklyReading.getId(), weeklyReading.getGoalCount());
    }
}
