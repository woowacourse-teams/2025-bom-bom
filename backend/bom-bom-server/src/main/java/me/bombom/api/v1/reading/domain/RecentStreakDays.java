package me.bombom.api.v1.reading.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import me.bombom.api.v1.common.util.DateUtils;

/**
 * 최근 N일의 날짜별 연속 읽기 상태를 판정한다.
 * 판정 순서: READ → PENDING → BEFORE_JOIN → SHIELD → REST → NO_ARTICLE → MISSED
 * 기간의 마지막 날(endDate)을 오늘로 본다.
 */
public class RecentStreakDays {

    private final StreakPeriod period;
    private final LocalDate joinDate;
    private final Set<LocalDate> readDates;
    private final Set<LocalDate> noArticleDates;
    private final Set<LocalDate> shieldUsedDates;
    private final Set<LocalDate> holidays;

    public RecentStreakDays(
            StreakPeriod period,
            LocalDate joinDate,
            Set<LocalDate> readDates,
            Set<LocalDate> noArticleDates,
            Set<LocalDate> shieldUsedDates,
            Set<LocalDate> holidays
    ) {
        this.period = Objects.requireNonNull(period, "period는 null일 수 없습니다.");
        this.joinDate = Objects.requireNonNull(joinDate, "joinDate는 null일 수 없습니다.");
        this.readDates = Set.copyOf(readDates);
        this.noArticleDates = Set.copyOf(noArticleDates);
        this.shieldUsedDates = Set.copyOf(shieldUsedDates);
        this.holidays = Set.copyOf(holidays);
    }

    public List<StreakDay> days() {
        return period.dates()
                .stream()
                .map(date -> new StreakDay(date, statusOf(date)))
                .toList();
    }

    public StreakDayStatus statusOf(LocalDate date) {
        if (!period.contains(date)) {
            throw new IllegalArgumentException("조회 기간 밖의 날짜입니다. date=" + date + ", period=" + period);
        }
        if (readDates.contains(date)) {
            return StreakDayStatus.READ;
        }
        if (date.equals(period.endDate())) {
            return StreakDayStatus.PENDING;
        }
        if (date.isBefore(joinDate)) {
            return StreakDayStatus.BEFORE_JOIN;
        }
        if (shieldUsedDates.contains(date)) {
            return StreakDayStatus.SHIELD;
        }
        if (DateUtils.isWeekend(date) || holidays.contains(date)) {
            return StreakDayStatus.REST;
        }
        if (noArticleDates.contains(date)) {
            return StreakDayStatus.NO_ARTICLE;
        }
        return StreakDayStatus.MISSED;
    }

    public record StreakDay(LocalDate date, StreakDayStatus status) {
    }
}
