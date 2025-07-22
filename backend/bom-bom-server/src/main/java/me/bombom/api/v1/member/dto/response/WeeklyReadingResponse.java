package me.bombom.api.v1.member.dto.response;

import me.bombom.api.v1.reading.domain.WeeklyReading;

public record WeeklyReadingResponse(
        int readCount,
        int goalCount
) {

    public static WeeklyReadingResponse from(WeeklyReading weeklyReading) {
        return new WeeklyReadingResponse(weeklyReading.getCurrentCount(), weeklyReading.getGoalCount());
    }
}
