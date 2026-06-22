package me.bombom.api.v1.reading.scheduler;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import me.bombom.api.v1.reading.service.ContinueReadingShieldService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class ReadingSchedulerTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @InjectMocks
    private ReadingScheduler readingScheduler;

    @Mock
    private ReadingService readingService;

    @Mock
    private ContinueReadingShieldService continueReadingShieldService;

    @Mock
    private Clock clock;

    @Test
    void daily_ResetReadingCount_스케줄러는_연속_및_읽기_정보를_초기화한다() {
        // when
        readingScheduler.dailyResetReadingCount();

        // then
        InOrder inOrder = inOrder(readingService, continueReadingShieldService);
        inOrder.verify(readingService, times(1)).resetContinueReadingCount();
        inOrder.verify(continueReadingShieldService, times(1)).resetMonthlyShieldsIfFirstDay();
        inOrder.verify(readingService, times(1)).resetTodayReadingCount();
    }

    @Test
    void weekly_ResetReadingCount_스케줄러는_주간_읽기_정보를_초기화한다() {
        // when
        readingScheduler.weeklyResetReadingCount();

        // then
        verify(readingService, times(1)).resetWeeklyReadingCount();
    }

    @Test
    void monthly_ResetReadingCount_스케줄러는_월간_읽기수를_연간에_반영하고_초기화한다() {
        // when
        readingScheduler.monthlyResetReadingCount();

        // then
        verify(readingService, times(1)).migrateMonthlyCountToYearlyAndReset();
    }

    @Test
    void 월간_랭킹_스케줄러는_월초_초기화_구간이면_랭킹을_갱신하지_않는다() {
        // given
        fixClockTo(LocalDateTime.of(2026, 6, 1, 0, 9));

        // when
        readingScheduler.tenMinutelyCalculateMemberRank();

        // then
        verify(readingService, never()).updateMonthlyRanking();
    }

    @Test
    void 월간_랭킹_스케줄러는_월초_0시10분부터_랭킹을_갱신한다() {
        // given
        fixClockTo(LocalDateTime.of(2026, 6, 1, 0, 10));

        // when
        readingScheduler.tenMinutelyCalculateMemberRank();

        // then
        verify(readingService, times(1)).updateMonthlyRanking();
    }

    @Test
    void 월간_랭킹_스케줄러는_월초_1시에는_랭킹을_갱신한다() {
        // given
        fixClockTo(LocalDateTime.of(2026, 6, 1, 1, 0));

        // when
        readingScheduler.tenMinutelyCalculateMemberRank();

        // then
        verify(readingService, times(1)).updateMonthlyRanking();
    }

    @Test
    void 월간_랭킹_스케줄러는_월초가_아니면_랭킹을_갱신한다() {
        // given
        fixClockTo(LocalDateTime.of(2026, 6, 2, 0, 5));

        // when
        readingScheduler.tenMinutelyCalculateMemberRank();

        // then
        verify(readingService, times(1)).updateMonthlyRanking();
    }

    @Test
    void 연속_읽기_랭킹_스케줄러는_스냅샷을_갱신한다() {
        // when
        readingScheduler.tenMinutelyCalculateContinueReadingRankingSnapshot();

        // then
        verify(readingService, times(1)).updateContinueReadingRankingSnapshot();
    }

    private void fixClockTo(LocalDateTime dateTime) {
        given(clock.instant()).willReturn(dateTime.atZone(SEOUL_ZONE).toInstant());
        given(clock.getZone()).willReturn(SEOUL_ZONE);
    }
}
