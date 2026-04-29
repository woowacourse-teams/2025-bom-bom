package me.bombom.api.v1.common.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DateUtils {

    public static boolean isWeekend(LocalDate date) {
        Objects.requireNonNull(date, "date는 null일 수 없습니다.");
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
