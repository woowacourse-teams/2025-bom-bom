package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.badge.service.BadgeService;
import me.bombom.api.v1.common.ContinueReadingRankingScheduleProperties;
import me.bombom.api.v1.common.MonthlyRankingScheduleProperties;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class ReadingServiceResetContinueReadingCountTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @InjectMocks
    private ReadingService readingService;

    @Mock
    private ReadingSnapshotMetaService readingSnapshotMetaService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private ContinueReadingRealtimeRepository continueReadingRepository;

    @Mock
    private ContinueReadingSnapshotRepository continueReadingRankingSnapshotRepository;

    @Mock
    private TodayReadingRepository todayReadingRepository;

    @Mock
    private WeeklyReadingRepository weeklyReadingRepository;

    @Mock
    private MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;

    @Mock
    private MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;

    @Mock
    private YearlyReadingRepository yearlyReadingRepository;

    @Mock
    private MonthlyRankingScheduleProperties scheduleProps;

    @Mock
    private ContinueReadingRankingScheduleProperties continueReadingRankingScheduleProperties;

    @Mock
    private BadgeService badgeService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private Clock clock;

    @Test
    void 어제가_토요일이면_연속_읽기_횟수를_초기화하지_않는다() {
        fixClockTo(LocalDate.of(2026, 4, 26)); // Sunday

        readingService.resetContinueReadingCount();

        verify(todayReadingRepository, never()).findTotalNonZeroAndCurrentZero();
    }

    @Test
    void 어제가_일요일이면_연속_읽기_횟수를_초기화하지_않는다() {
        fixClockTo(LocalDate.of(2026, 4, 27)); // Monday

        readingService.resetContinueReadingCount();

        verify(todayReadingRepository, never()).findTotalNonZeroAndCurrentZero();
    }

    @Test
    void 어제가_평일이면_기존_조건대로_연속_읽기_횟수를_초기화한다() {
        fixClockTo(LocalDate.of(2026, 4, 28)); // Tuesday
        TodayReading todayReading = TodayReading.builder()
                .memberId(1L)
                .totalCount(3)
                .currentCount(0)
                .build();
        ContinueReadingRealtime continueReading = ContinueReadingRealtime.builder()
                .memberId(1L)
                .dayCount(10)
                .build();
        given(todayReadingRepository.findTotalNonZeroAndCurrentZero()).willReturn(List.of(todayReading));
        given(continueReadingRepository.findByMemberId(1L)).willReturn(Optional.of(continueReading));

        readingService.resetContinueReadingCount();

        assertThat(continueReading.getDayCount()).isZero();
    }

    private void fixClockTo(LocalDate date) {
        given(clock.instant()).willReturn(date.atStartOfDay(SEOUL_ZONE).toInstant());
        given(clock.getZone()).willReturn(SEOUL_ZONE);
    }
}
