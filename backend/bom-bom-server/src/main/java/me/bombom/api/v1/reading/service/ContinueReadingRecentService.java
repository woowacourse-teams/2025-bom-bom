package me.bombom.api.v1.reading.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.common.holiday.domain.Holiday;
import me.bombom.api.v1.common.holiday.repository.HolidayRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResult;
import me.bombom.api.v1.reading.domain.ContinueReadingDailyResultType;
import me.bombom.api.v1.reading.domain.RecentStreakDays;
import me.bombom.api.v1.reading.domain.StreakPeriod;
import me.bombom.api.v1.reading.dto.response.RecentContinueReadingResponse;
import me.bombom.api.v1.reading.dto.response.StreakShieldResponse;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 투데이 화면 연속 읽기 바를 위한 최근 N일 연속 읽기 기록 조회.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContinueReadingRecentService {

    private final ContinueReadingRealtimeRepository continueReadingRealtimeRepository;
    private final ContinueReadingSnapshotRepository continueReadingSnapshotRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final ArticleReadHistoryRepository articleReadHistoryRepository;
    private final HolidayRepository holidayRepository;
    private final ContinueReadingShieldService continueReadingShieldService;
    private final ContinueReadingDailyResultService continueReadingDailyResultService;
    private final Clock clock;

    public RecentContinueReadingResponse getRecentContinueReading(Member member, int days) {
        Long memberId = member.getId();
        StreakPeriod period = StreakPeriod.recent(LocalDate.now(clock), days);

        int dayCount = findDayCount(memberId);
        long rank = findRank(memberId);
        StreakShieldResponse streakShield = continueReadingShieldService.getStreakShield(memberId);
        RecentStreakDays recentStreakDays = createRecentStreakDays(member, period);

        return RecentContinueReadingResponse.of(dayCount, rank, streakShield, recentStreakDays.days());
    }

    private RecentStreakDays createRecentStreakDays(Member member, StreakPeriod period) {
        Long memberId = member.getId();
        List<ContinueReadingDailyResult> dailyResults = continueReadingDailyResultService.findResults(
                memberId,
                period.startDate(),
                period.endDate()
        );
        return new RecentStreakDays(
                period,
                member.getCreatedAt().toLocalDate(),
                findReadDates(memberId, period, dailyResults),
                datesOf(dailyResults, ContinueReadingDailyResultType.NO_ARTICLE),
                continueReadingShieldService.findUsedDates(memberId, period.startDate(), period.endDate()),
                findHolidays(period)
        );
    }

    /**
     * 읽은 날 = 읽은 기록(article_read_history) ∪ 0시에 기록한 READ ∪ 오늘 연속 읽기에 반영된 읽기.
     * 가이드 메일 읽기는 article_read_history를 남기지 않아 today_reading과 일별 결과로 보완한다.
     */
    private Set<LocalDate> findReadDates(
            Long memberId,
            StreakPeriod period,
            List<ContinueReadingDailyResult> dailyResults
    ) {
        Set<LocalDate> readDates = new HashSet<>(articleReadHistoryRepository.findReadDates(
                memberId,
                period.startInclusive(),
                period.endExclusive()
        ));
        readDates.addAll(datesOf(dailyResults, ContinueReadingDailyResultType.READ));
        if (hasReadToday(memberId)) {
            readDates.add(period.endDate());
        }
        return readDates;
    }

    private boolean hasReadToday(Long memberId) {
        return todayReadingRepository.findByMemberId(memberId)
                .map(todayReading -> todayReading.getReadCount() > 0)
                .orElse(false);
    }

    private Set<LocalDate> datesOf(List<ContinueReadingDailyResult> dailyResults, ContinueReadingDailyResultType type) {
        return dailyResults.stream()
                .filter(dailyResult -> dailyResult.getResult() == type)
                .map(ContinueReadingDailyResult::getResultDate)
                .collect(Collectors.toSet());
    }

    private Set<LocalDate> findHolidays(StreakPeriod period) {
        return holidayRepository.findAllByDateBetween(period.startDate(), period.endDate())
                .stream()
                .map(Holiday::getDate)
                .collect(Collectors.toSet());
    }

    private int findDayCount(Long memberId) {
        return continueReadingRealtimeRepository.findByMemberId(memberId)
                .orElseThrow(() -> entityNotFound(memberId, "ContinueReadingRealtime"))
                .getDayCount();
    }

    private long findRank(Long memberId) {
        return continueReadingSnapshotRepository.findByMemberId(memberId)
                .orElseThrow(() -> entityNotFound(memberId, "ContinueReadingSnapshot"))
                .getRankOrder();
    }

    private CIllegalArgumentException entityNotFound(Long memberId, String entityType) {
        return new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                .addContext(ErrorContextKeys.ENTITY_TYPE, entityType);
    }
}
