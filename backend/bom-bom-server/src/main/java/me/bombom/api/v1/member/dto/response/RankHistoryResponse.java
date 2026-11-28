package me.bombom.api.v1.member.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.YearMonth;

public record RankHistoryResponse(

        @NotNull
        String month,

        @NotNull
        String label,

        @Schema(requiredMode = Schema.RequiredMode.REQUIRED)
        long rank,

        @Schema(
                requiredMode = Schema.RequiredMode.REQUIRED,
                description = "그 달의 값. reading은 그 달 읽은 아티클 수, streak는 다음 달 1일 0시 확정 시점의 연속 읽기 일수"
        )
        int value
) {

    public static RankHistoryResponse of(
            LocalDate period,
            long rank,
            int value,
            LocalDate today
    ) {
        String month = YearMonth.from(period).toString();
        String label = period.getYear() == today.getYear()
                ? period.getMonthValue() + "월"
                : String.format("%02d.%02d", period.getYear() % 100, period.getMonthValue());
        return new RankHistoryResponse(
                month,
                label,
                rank,
                value
        );
    }
}
