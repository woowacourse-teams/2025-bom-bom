package me.bombom.api.v1.reading.dto.response;

import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;

public record ReadingInformationResponse(
        int streakReadDay,
        TodayReadingResponse today,
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
