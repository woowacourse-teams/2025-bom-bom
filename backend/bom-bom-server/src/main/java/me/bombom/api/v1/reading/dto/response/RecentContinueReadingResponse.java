package me.bombom.api.v1.reading.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import me.bombom.api.v1.reading.domain.RecentStreakDays.StreakDay;

public record RecentContinueReadingResponse(

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "현재 연속 읽기 일수 (실시간)")
        int dayCount,

        @Schema(
                requiredMode = Schema.RequiredMode.REQUIRED,
                description = "연속 읽기 순위. 랭킹 스냅샷 값이라 갱신 주기(약 10분)만큼 늦을 수 있음"
        )
        long rank,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        StreakShieldResponse streakShield,

        @NotNull
        @Schema(description = "날짜별 상태 목록. 오늘을 포함한 최근 N일(쿼리 파라미터 days), 날짜 오름차순")
        List<StreakDayResponse> days
) {

    public static RecentContinueReadingResponse of(
            int dayCount,
            long rank,
            StreakShieldResponse streakShield,
            List<StreakDay> streakDays
    ) {
        return new RecentContinueReadingResponse(
                dayCount,
                rank,
                streakShield,
                streakDays.stream()
                        .map(StreakDayResponse::from)
                        .toList()
        );
    }
}
