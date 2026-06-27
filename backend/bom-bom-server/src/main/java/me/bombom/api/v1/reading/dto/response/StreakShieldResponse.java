package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.reading.domain.ContinueReadingShield;

public record StreakShieldResponse(

        StreakShieldStatus status,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int remainingCount,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        int monthlyLimit
) {

    public static StreakShieldResponse of(ContinueReadingShield shield, int monthlyLimit) {
        int remainingCount = shield.getRemainingCount();
        return new StreakShieldResponse(
                StreakShieldStatus.from(remainingCount),
                remainingCount,
                monthlyLimit
        );
    }
}
