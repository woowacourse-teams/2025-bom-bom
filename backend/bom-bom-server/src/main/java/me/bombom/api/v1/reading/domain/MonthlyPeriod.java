package me.bombom.api.v1.reading.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.stream.Stream;

public record MonthlyPeriod(
        LocalDateTime start,
        LocalDateTime end
) {

    public static MonthlyPeriod from(YearMonth yearMonth) {
        return new MonthlyPeriod(
                yearMonth.atDay(1).atStartOfDay(),
                yearMonth.plusMonths(1).atDay(1).atStartOfDay()
        );
    }

    public Stream<LocalDate> dates() {
        return start.toLocalDate().datesUntil(end.toLocalDate());
    }
}
