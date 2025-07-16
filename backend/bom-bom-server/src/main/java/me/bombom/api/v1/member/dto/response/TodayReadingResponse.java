package me.bombom.api.v1.member.dto.response;

import me.bombom.api.v1.member.domain.TodayReading;

public record TodayReadingResponse(
        int readCount,
        int totalCount
) {

    public static TodayReadingResponse from(TodayReading todayReading) {
        return new TodayReadingResponse(todayReading.getCurrentCount(), todayReading.getTotalCount());
    }
}
