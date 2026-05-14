package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.reading.domain.TodayReading;

public record TodayReadingResponse(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int readCount,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int totalCount
) {

    public static TodayReadingResponse from(TodayReading todayReading) {
        return new TodayReadingResponse(todayReading.getCurrentCount(), todayReading.getTotalCount());
    }
}
