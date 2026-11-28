package me.bombom.api.v1.reading.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * 최근 연속 읽기 기록을 조회하는 기간. 오늘(endDate)을 포함한 최근 N일이다.
 */
public record StreakPeriod(LocalDate startDate, LocalDate endDate) {

    public static final int MIN_DAYS = 1;
    public static final int MAX_DAYS = 31;
    public static final String DEFAULT_DAYS = "7";
    public static final String DAYS_RANGE_MESSAGE = "days는 " + MIN_DAYS + " 이상 " + MAX_DAYS + " 이하의 값이어야 합니다.";

    public StreakPeriod {
        Objects.requireNonNull(startDate, "startDate는 null일 수 없습니다.");
        Objects.requireNonNull(endDate, "endDate는 null일 수 없습니다.");
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("startDate는 endDate보다 늦을 수 없습니다.");
        }
    }

    public static StreakPeriod recent(LocalDate today, int days) {
        if (days < MIN_DAYS || days > MAX_DAYS) {
            throw new IllegalArgumentException(DAYS_RANGE_MESSAGE);
        }
        return new StreakPeriod(today.minusDays(days - 1L), today);
    }

    public List<LocalDate> dates() {
        return startDate.datesUntil(endDate.plusDays(1)).toList();
    }

    public boolean contains(LocalDate date) {
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    public LocalDateTime startInclusive() {
        return startDate.atStartOfDay();
    }

    public LocalDateTime endExclusive() {
        return endDate.plusDays(1).atStartOfDay();
    }
}
