package me.bombom.api.v1.reading.dto.response;

import me.bombom.api.v1.reading.domain.WeeklyReading;

public record WeeklyCurrentCountResponse(
        Long weeklyReadingId,
        int currentCount
) {

    public static WeeklyCurrentCountResponse from(WeeklyReading weeklyReading) {
        return new WeeklyCurrentCountResponse(weeklyReading.getId(), weeklyReading.getCurrentCount());
    }
}
