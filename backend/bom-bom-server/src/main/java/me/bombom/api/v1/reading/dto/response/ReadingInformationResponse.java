package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record ReadingInformationResponse(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int streakReadDay,

        @NotNull
        TodayReadingResponse today,

        @NotNull
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
