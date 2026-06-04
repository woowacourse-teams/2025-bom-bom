package me.bombom.api.v1.reading.service;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReadingServiceResetContinueReadingCountTest {

    private static final ZoneId SEOUL_ZONE = ZoneId.of("Asia/Seoul");

    @InjectMocks
    private ReadingService readingService;

    @Mock
    private ContinueReadingRealtimeRepository continueReadingRepository;

    @Mock
    private TodayReadingRepository todayReadingRepository;

    @Mock
    private HolidayRepository holidayRepository;

    @Mock
    private ContinueReadingShieldService continueReadingShieldService;

    @Mock
    private Clock clock;

    @Test
    void 어제가_토요일이면_연속_읽기_횟수를_초기화하지_않는다() {
        fixClockTo(LocalDate.of(2026, 4, 26)); // Sunday

        readingService.resetContinueReadingCount();

        verify(todayReadingRepository, never()).findTotalNonZeroAndReadZero();
        verify(continueReadingShieldService, never()).useShield(anyLong(), any());
    }

    @Test
    void 어제가_일요일이면_연속_읽기_횟수를_초기화하지_않는다() {
        fixClockTo(LocalDate.of(2026, 4, 27)); // Monday

        readingService.resetContinueReadingCount();

        verify(todayReadingRepository, never()).findTotalNonZeroAndReadZero();
        verify(continueReadingShieldService, never()).useShield(anyLong(), any());
    }

    @Test
    void 어제가_공휴일이면_연속_읽기_횟수를_초기화하지_않는다() {
        LocalDate holiday = LocalDate.of(2026, 5, 5);
        fixClockTo(holiday.plusDays(1));
        given(holidayRepository.existsByDate(holiday)).willReturn(true);

        readingService.resetContinueReadingCount();

        verify(todayReadingRepository, never()).findTotalNonZeroAndReadZero();
        verify(continueReadingShieldService, never()).useShield(anyLong(), any());
    }

    @Test
    void 어제가_평일이고_보호막이_없으면_연속_읽기_횟수를_초기화하고_최대_스트릭은_유지한다() {
        fixClockTo(LocalDate.of(2026, 4, 28)); // Tuesday
        LocalDate targetDate = LocalDate.of(2026, 4, 27);
        given(holidayRepository.existsByDate(targetDate)).willReturn(false);
        TodayReading todayReading = TodayReading.builder()
                .memberId(1L)
                .totalCount(3)
                .currentCount(0)
                .readCount(0)
                .build();
        ContinueReadingRealtime continueReading = ContinueReadingRealtime.builder()
                .memberId(1L)
                .dayCount(10)
                .maxDayCount(15)
                .build();
        given(todayReadingRepository.findTotalNonZeroAndReadZero()).willReturn(List.of(todayReading));
        given(continueReadingRepository.findByMemberId(1L)).willReturn(Optional.of(continueReading));
        given(continueReadingShieldService.useShield(1L, targetDate)).willReturn(false);

        readingService.resetContinueReadingCount();

        assertSoftly(softly -> {
            softly.assertThat(continueReading.getDayCount()).isZero();
            softly.assertThat(continueReading.getMaxDayCount()).isEqualTo(15);
        });
    }

    @Test
    void 어제가_평일이고_보호막이_있으면_연속_읽기_횟수를_유지한다() {
        fixClockTo(LocalDate.of(2026, 4, 28)); // Tuesday
        LocalDate targetDate = LocalDate.of(2026, 4, 27);
        given(holidayRepository.existsByDate(targetDate)).willReturn(false);
        TodayReading todayReading = TodayReading.builder()
                .memberId(1L)
                .totalCount(3)
                .currentCount(0)
                .readCount(0)
                .build();
        ContinueReadingRealtime continueReading = ContinueReadingRealtime.builder()
                .memberId(1L)
                .dayCount(10)
                .maxDayCount(15)
                .build();
        given(todayReadingRepository.findTotalNonZeroAndReadZero()).willReturn(List.of(todayReading));
        given(continueReadingRepository.findByMemberId(1L)).willReturn(Optional.of(continueReading));
        given(continueReadingShieldService.useShield(1L, targetDate)).willReturn(true);

        readingService.resetContinueReadingCount();

        assertSoftly(softly -> {
            softly.assertThat(continueReading.getDayCount()).isEqualTo(10);
            softly.assertThat(continueReading.getMaxDayCount()).isEqualTo(15);
        });
    }

    @Test
    void 연속_읽기_횟수가_0이면_보호막을_차감하지_않는다() {
        fixClockTo(LocalDate.of(2026, 4, 28)); // Tuesday
        LocalDate targetDate = LocalDate.of(2026, 4, 27);
        given(holidayRepository.existsByDate(targetDate)).willReturn(false);
        TodayReading todayReading = TodayReading.builder()
                .memberId(1L)
                .totalCount(3)
                .currentCount(0)
                .readCount(0)
                .build();
        ContinueReadingRealtime continueReading = ContinueReadingRealtime.builder()
                .memberId(1L)
                .dayCount(0)
                .maxDayCount(15)
                .build();
        given(todayReadingRepository.findTotalNonZeroAndReadZero()).willReturn(List.of(todayReading));
        given(continueReadingRepository.findByMemberId(1L)).willReturn(Optional.of(continueReading));

        readingService.resetContinueReadingCount();

        assertSoftly(softly -> {
            softly.assertThat(continueReading.getDayCount()).isZero();
            softly.assertThat(continueReading.getMaxDayCount()).isEqualTo(15);
        });
        verify(continueReadingShieldService, never()).useShield(anyLong(), any());
    }

    @Test
    void 어제가_평일이어도_오늘_읽은_아티클이_있으면_연속_읽기_횟수를_초기화하지_않는다() {
        fixClockTo(LocalDate.of(2026, 4, 28)); // Tuesday
        given(holidayRepository.existsByDate(LocalDate.of(2026, 4, 27))).willReturn(false);
        given(todayReadingRepository.findTotalNonZeroAndReadZero()).willReturn(List.of());

        readingService.resetContinueReadingCount();

        verify(continueReadingRepository, never()).findByMemberId(1L);
        verify(continueReadingShieldService, never()).useShield(anyLong(), any());
    }

    private void fixClockTo(LocalDate date) {
        given(clock.instant()).willReturn(date.atStartOfDay(SEOUL_ZONE).toInstant());
        given(clock.getZone()).willReturn(SEOUL_ZONE);
    }
}
