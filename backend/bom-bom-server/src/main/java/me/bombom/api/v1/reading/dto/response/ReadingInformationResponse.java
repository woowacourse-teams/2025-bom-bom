package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record ReadingInformationResponse(
        @Schema(type = "integer", format = "int32", description = "연속 읽기 일수", required = true)
        int streakReadDay,

        @NotNull
        @Schema(type = "object", description = "오늘 읽기 정보", required = true)
        TodayReadingResponse today,

        @NotNull
        @Schema(type = "object", description = "주간 읽기 정보", required = true)
        WeeklyReadingResponse weekly
) {

    public static ReadingInformationResponse of(
            ContinueReading continueReading,
            TodayReading todayReading,
            WeeklyReading weeklyReading
    ) {
        return new ReadingInformationResponse(
                continueReading.getDayCount(),
                TodayReadingResponse.from(todayReading),
                WeeklyReadingResponse.from(weeklyReading)
        );
    }
}
