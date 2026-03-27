package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record ReadingInformationResponse(

        @Schema(
                type = "integer",
                format = "int32",
                description = "연속 읽기 일수",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        int streakReadDay,

        @NotNull
        @Schema(
                type = "object",
                description = "오늘 읽기 정보",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        TodayReadingResponse today,

        @NotNull
        @Schema(
                type = "object",
                description = "주간 읽기 정보",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        WeeklyReadingResponse weekly
) {

    public static ReadingInformationResponse of(
            ContinueReadingRealtime continueReading,
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
