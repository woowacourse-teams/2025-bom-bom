package me.bombom.api.v1.member.dto.response;

import me.bombom.api.v1.member.domain.ContinueReading;
import me.bombom.api.v1.member.domain.TodayReading;
import me.bombom.api.v1.member.domain.WeeklyReading;

public record ReadingInformationResponse(
        int streakReadDay,
        TodayReadingResponse today,
        WeeklyReadingResponse weekly
) {

    public static ReadingInformationResponse from(
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
