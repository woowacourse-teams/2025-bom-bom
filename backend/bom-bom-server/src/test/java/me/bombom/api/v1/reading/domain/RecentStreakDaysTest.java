package me.bombom.api.v1.reading.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import java.time.LocalDate;
import java.util.Set;
import me.bombom.api.v1.reading.domain.RecentStreakDays.StreakDay;
import org.junit.jupiter.api.Test;

class RecentStreakDaysTest {

    // 2026-11-27(금)
    private static final LocalDate TODAY = LocalDate.of(2026, 11, 27);
    private static final LocalDate JOIN_DATE = LocalDate.of(2026, 1, 1);
    private static final StreakPeriod WEEK = StreakPeriod.recent(TODAY, 7);

    @Test
    void 기간의_날짜를_오름차순으로_반환한다() {
        RecentStreakDays recentStreakDays = streakDays(WEEK, Set.of(), Set.of(), Set.of(), Set.of());

        assertThat(recentStreakDays.days())
                .extracting(StreakDay::date)
                .containsExactly(
                        LocalDate.of(2026, 11, 21),
                        LocalDate.of(2026, 11, 22),
                        LocalDate.of(2026, 11, 23),
                        LocalDate.of(2026, 11, 24),
                        LocalDate.of(2026, 11, 25),
                        LocalDate.of(2026, 11, 26),
                        TODAY
                );
    }

    @Test
    void 읽음_보호막_쉬는날_도착없음_안읽음_오늘_미읽음이_섞인_날짜별_상태를_판정한다() {
        RecentStreakDays recentStreakDays = streakDays(
                WEEK,
                Set.of(LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 26)),
                Set.of(LocalDate.of(2026, 11, 25)),
                Set.of(LocalDate.of(2026, 11, 23)),
                Set.of(LocalDate.of(2026, 11, 24))
        );

        assertThat(recentStreakDays.days())
                .extracting(StreakDay::date, StreakDay::status)
                .containsExactly(
                        tuple(LocalDate.of(2026, 11, 21), StreakDayStatus.REST),       // 토요일
                        tuple(LocalDate.of(2026, 11, 22), StreakDayStatus.READ),       // 일요일에 읽음
                        tuple(LocalDate.of(2026, 11, 23), StreakDayStatus.SHIELD),
                        tuple(LocalDate.of(2026, 11, 24), StreakDayStatus.REST),       // 공휴일
                        tuple(LocalDate.of(2026, 11, 25), StreakDayStatus.NO_ARTICLE), // 받은 아티클 없음
                        tuple(LocalDate.of(2026, 11, 26), StreakDayStatus.READ),
                        tuple(TODAY, StreakDayStatus.PENDING)
                );
    }

    @Test
    void 아무_기록이_없는_평일은_MISSED다() {
        RecentStreakDays recentStreakDays = streakDays(WEEK, Set.of(), Set.of(), Set.of(), Set.of());

        assertThat(recentStreakDays.statusOf(LocalDate.of(2026, 11, 25))).isEqualTo(StreakDayStatus.MISSED);
    }

    @Test
    void 오늘_읽었으면_READ다() {
        RecentStreakDays recentStreakDays = streakDays(WEEK, Set.of(TODAY), Set.of(), Set.of(), Set.of());

        assertThat(recentStreakDays.statusOf(TODAY)).isEqualTo(StreakDayStatus.READ);
    }

    @Test
    void 가입일_이전은_BEFORE_JOIN이고_가입일부터는_일반_판정을_한다() {
        RecentStreakDays recentStreakDays = new RecentStreakDays(
                WEEK, LocalDate.of(2026, 11, 25), Set.of(), Set.of(), Set.of(), Set.of()
        );

        assertThat(recentStreakDays.days())
                .extracting(StreakDay::status)
                .containsExactly(
                        StreakDayStatus.BEFORE_JOIN,
                        StreakDayStatus.BEFORE_JOIN,
                        StreakDayStatus.BEFORE_JOIN,
                        StreakDayStatus.BEFORE_JOIN,
                        StreakDayStatus.MISSED,
                        StreakDayStatus.MISSED,
                        StreakDayStatus.PENDING
                );
    }

    @Test
    void 보호막을_쓴_날은_주말이어도_SHIELD가_우선한다() {
        LocalDate saturday = LocalDate.of(2026, 11, 21);
        RecentStreakDays recentStreakDays = streakDays(WEEK, Set.of(), Set.of(), Set.of(saturday), Set.of());

        assertThat(recentStreakDays.statusOf(saturday)).isEqualTo(StreakDayStatus.SHIELD);
    }

    @Test
    void 읽은_날은_다른_기록이_있어도_READ가_우선한다() {
        LocalDate date = LocalDate.of(2026, 11, 25);
        RecentStreakDays recentStreakDays = streakDays(WEEK, Set.of(date), Set.of(date), Set.of(date), Set.of());

        assertThat(recentStreakDays.statusOf(date)).isEqualTo(StreakDayStatus.READ);
    }

    @Test
    void 공휴일에_도착_없음_기록이_있어도_REST다() {
        LocalDate holiday = LocalDate.of(2026, 11, 24);
        RecentStreakDays recentStreakDays = streakDays(WEEK, Set.of(), Set.of(holiday), Set.of(), Set.of(holiday));

        assertThat(recentStreakDays.statusOf(holiday)).isEqualTo(StreakDayStatus.REST);
    }

    @Test
    void 기간_밖의_날짜는_판정하지_않는다() {
        RecentStreakDays recentStreakDays = streakDays(WEEK, Set.of(), Set.of(), Set.of(), Set.of());

        assertThatThrownBy(() -> recentStreakDays.statusOf(LocalDate.of(2026, 11, 20)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void 가입일은_필수다() {
        assertThatThrownBy(() -> new RecentStreakDays(WEEK, null, Set.of(), Set.of(), Set.of(), Set.of()))
                .isInstanceOf(NullPointerException.class);
    }

    private RecentStreakDays streakDays(
            StreakPeriod period,
            Set<LocalDate> readDates,
            Set<LocalDate> noArticleDates,
            Set<LocalDate> shieldUsedDates,
            Set<LocalDate> holidays
    ) {
        return new RecentStreakDays(period, JOIN_DATE, readDates, noArticleDates, shieldUsedDates, holidays);
    }
}
