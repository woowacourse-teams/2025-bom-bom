package me.bombom.api.v1.reading.service;

import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.bookmark.repository.BookmarkRepository;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.domain.MonthlyPeriod;
import me.bombom.api.v1.reading.dto.DailyReadCount;
import me.bombom.api.v1.reading.dto.FrequentReadNewsletter;
import me.bombom.api.v1.reading.dto.ReadCountComparison;
import me.bombom.api.v1.reading.mapper.MonthlyReportResponseMapper;
import me.bombom.openapi.model.MonthlyReportDashboardRequest;
import me.bombom.openapi.model.MonthlyReportRequest;
import me.bombom.openapi.model.ReadingCalendarDayResponse;
import me.bombom.openapi.model.ReadingDashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyReportService {

    private final ArticleReadHistoryRepository articleReadHistoryRepository;
    private final BookmarkRepository bookmarkRepository;
    private final MonthlyReportResponseMapper responseMapper;

    public List<ReadingCalendarDayResponse> getReadingCalendar(Member member, MonthlyReportRequest request) {
        MonthlyPeriod period = MonthlyPeriod.from(YearMonth.of(request.year(), request.month()));
        List<DailyReadCount> dailyReadCounts = countDailyReads(member, period);

        return responseMapper.toReadingCalendar(period, dailyReadCounts);
    }

    public ReadingDashboardResponse getReadingDashboard(Member member, MonthlyReportDashboardRequest request) {
        YearMonth targetMonth = YearMonth.of(request.year(), request.month());
        MonthlyPeriod currentPeriod = MonthlyPeriod.from(targetMonth);
        MonthlyPeriod previousPeriod = MonthlyPeriod.from(targetMonth.minusMonths(1));

        ReadCountComparison readCountComparison = countReads(member, previousPeriod, currentPeriod);
        long currentReadCount = readCountComparison.currentReadCount();
        long previousReadCount = readCountComparison.previousReadCount();
        long bookmarkCount = countBookmarks(member, currentPeriod);
        List<FrequentReadNewsletter> frequentReadNewsletters = findFrequentReadNewsletters(
                member,
                currentPeriod,
                request.limit()
        );

        return responseMapper.toReadingDashboard(
                currentReadCount,
                previousReadCount,
                bookmarkCount,
                frequentReadNewsletters
        );
    }

    private List<DailyReadCount> countDailyReads(Member member, MonthlyPeriod period) {
        return articleReadHistoryRepository.countDailyReads(
                member.getId(),
                period.startInclusive(),
                period.endExclusive()
        );
    }

    private ReadCountComparison countReads(
            Member member,
            MonthlyPeriod previousPeriod,
            MonthlyPeriod currentPeriod
    ) {
        return articleReadHistoryRepository.countReadsInPeriods(
                member.getId(),
                previousPeriod.startInclusive(),
                currentPeriod.startInclusive(),
                currentPeriod.endExclusive()
        );
    }

    private long countBookmarks(Member member, MonthlyPeriod period) {
        return bookmarkRepository.countBookmarksInPeriod(
                member.getId(),
                period.startInclusive(),
                period.endExclusive()
        );
    }

    private List<FrequentReadNewsletter> findFrequentReadNewsletters(
            Member member,
            MonthlyPeriod period,
            int limit
    ) {
        return articleReadHistoryRepository.findFrequentReadNewsletters(
                member.getId(),
                period.startInclusive(),
                period.endExclusive(),
                limit
        );
    }
}
