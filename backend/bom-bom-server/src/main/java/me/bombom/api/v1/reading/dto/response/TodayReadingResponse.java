package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.reading.domain.TodayReading;

public record TodayReadingResponse(

        @Schema(type = "integer", format = "int32", description = "읽은 아티클 수", required = true)
        int readCount,
        
        @Schema(type = "integer", format = "int32", description = "전체 아티클 수", required = true)
        int totalCount
) {

    public static TodayReadingResponse from(TodayReading todayReading) {
        return new TodayReadingResponse(todayReading.getCurrentCount(), todayReading.getTotalCount());
    }
}
