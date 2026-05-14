package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

public record MemberMonthlyReadingCountResponse(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int readCount
) {

    public static MemberMonthlyReadingCountResponse from(int count) {
        return new MemberMonthlyReadingCountResponse(count);
    }
}
