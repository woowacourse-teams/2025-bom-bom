package me.bombom.api.v1.member.dto.response;

import java.time.LocalDate;

public record RankHistoryResponse(
        String month,
        String label,
        long rank
) {

    public static RankHistoryResponse of(
            LocalDate period,
            long rank,
            LocalDate today
    ) {
        String month = period.toString().substring(0, 7);
        String label = period.getYear() == today.getYear()
                ? period.getMonthValue() + "월"
                : String.format("%02d.%02d", period.getYear() % 100, period.getMonthValue());
        return new RankHistoryResponse(
                month,
                label,
                rank
        );
    }
}
