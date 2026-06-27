package me.bombom.api.v1.reading.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;

class ReadingCounterDomainTest {

    @Test
    void TodayReading_create는_초기값을_설정하고_증가와_초기화를_수행한다() {
        // given
        TodayReading todayReading = TodayReading.create(1L);

        // when
        todayReading.increaseCurrentCount();
        todayReading.increaseReadCount();

        // then
        assertSoftly(softly -> {
            softly.assertThat(todayReading.getMemberId()).isEqualTo(1L);
            softly.assertThat(todayReading.getTotalCount()).isEqualTo(3);
            softly.assertThat(todayReading.getCurrentCount()).isEqualTo(1);
            softly.assertThat(todayReading.getReadCount()).isEqualTo(1);
        });

        // when
        todayReading.resetCount();

        // then
        assertSoftly(softly -> {
            softly.assertThat(todayReading.getTotalCount()).isZero();
            softly.assertThat(todayReading.getCurrentCount()).isZero();
            softly.assertThat(todayReading.getReadCount()).isZero();
        });
    }

    @Test
    void WeeklyReading_create는_기본_목표를_설정하고_목표와_현재값을_변경한다() {
        // given
        WeeklyReading weeklyReading = WeeklyReading.create(1L);

        // when
        weeklyReading.increaseCurrentCount();
        weeklyReading.updateGoalCount(5);

        // then
        assertSoftly(softly -> {
            softly.assertThat(weeklyReading.getMemberId()).isEqualTo(1L);
            softly.assertThat(weeklyReading.getGoalCount()).isEqualTo(5);
            softly.assertThat(weeklyReading.getCurrentCount()).isEqualTo(1);
        });

        // when
        weeklyReading.resetCurrentCount();

        // then
        assertThat(weeklyReading.getCurrentCount()).isZero();
    }

    @Test
    void MonthlyReadingRealtime은_현재값을_증가시킨다() {
        // given
        MonthlyReadingRealtime monthlyReadingRealtime = MonthlyReadingRealtime.create(1L);

        // when
        monthlyReadingRealtime.increaseCurrentCount();
        monthlyReadingRealtime.increaseCurrentCount();

        // then
        assertSoftly(softly -> {
            softly.assertThat(monthlyReadingRealtime.getMemberId()).isEqualTo(1L);
            softly.assertThat(monthlyReadingRealtime.getCurrentCount()).isEqualTo(2);
        });
    }

    @Test
    void MonthlyReadingSnapshot은_초기_랭킹값을_보관하고_현재값을_초기화한다() {
        // given
        MonthlyReadingSnapshot snapshot = MonthlyReadingSnapshot.builder()
                .memberId(1L)
                .currentCount(7)
                .rankOrder(10L)
                .nextRankDifference(3L)
                .build();

        // when
        snapshot.resetCurrentCount();

        // then
        assertSoftly(softly -> {
            softly.assertThat(snapshot.getMemberId()).isEqualTo(1L);
            softly.assertThat(snapshot.getCurrentCount()).isZero();
            softly.assertThat(snapshot.getRankOrder()).isEqualTo(10L);
            softly.assertThat(snapshot.getNextRankDifference()).isEqualTo(3L);
        });
    }

    @Test
    void YearlyReading은_연간_읽기수를_증가시키고_초기화한다() {
        // given
        YearlyReading yearlyReading = YearlyReading.create(1L, 2026);

        // when
        yearlyReading.increaseCurrentCount(3);

        // then
        assertSoftly(softly -> {
            softly.assertThat(yearlyReading.getMemberId()).isEqualTo(1L);
            softly.assertThat(yearlyReading.getReadingYear()).isEqualTo(2026);
            softly.assertThat(yearlyReading.getCurrentCount()).isEqualTo(3);
        });

        // when
        yearlyReading.resetCurrentCount();

        // then
        assertThat(yearlyReading.getCurrentCount()).isZero();
    }

    @Test
    void ContinueReadingRealtime은_연속일과_최대연속일을_갱신한다() {
        // given
        ContinueReadingRealtime realtime = ContinueReadingRealtime.builder()
                .memberId(1L)
                .dayCount(2)
                .maxDayCount(1)
                .build();

        // when
        realtime.increaseDayCount();

        // then
        assertSoftly(softly -> {
            softly.assertThat(realtime.hasActiveStreak()).isTrue();
            softly.assertThat(realtime.getDayCount()).isEqualTo(3);
            softly.assertThat(realtime.getMaxDayCount()).isEqualTo(3);
        });

        // when
        realtime.resetDayCount();

        // then
        assertSoftly(softly -> {
            softly.assertThat(realtime.hasActiveStreak()).isFalse();
            softly.assertThat(realtime.getDayCount()).isZero();
            softly.assertThat(realtime.getMaxDayCount()).isEqualTo(3);
        });
    }

    @Test
    void ContinueReadingRealtime은_maxDayCount가_null이면_dayCount를_최대값으로_사용한다() {
        // when
        ContinueReadingRealtime realtime = ContinueReadingRealtime.builder()
                .memberId(1L)
                .dayCount(4)
                .maxDayCount(null)
                .build();

        // then
        assertSoftly(softly -> {
            softly.assertThat(realtime.getDayCount()).isEqualTo(4);
            softly.assertThat(realtime.getMaxDayCount()).isEqualTo(4);
        });
    }
}
