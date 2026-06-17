package me.bombom.api.v1.member.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.repository.ArticleReadHistoryRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.CategoryReadCount;
import me.bombom.api.v1.member.dto.response.CategoryStatsResponse;
import me.bombom.api.v1.member.dto.response.RankCardResponse;
import me.bombom.api.v1.member.dto.response.RankHistoryResponse;
import me.bombom.api.v1.member.dto.response.RankSummaryResponse;
import me.bombom.api.v1.member.enums.MyPageRankType;
import me.bombom.api.v1.reading.domain.ContinueReadingRankHistory;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingRankHistory;
import me.bombom.api.v1.reading.repository.ContinueReadingRankHistoryRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRankHistoryRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MyPageService {

    private static final int RANK_HISTORY_LIMIT = 6;

    private final ContinueReadingRealtimeRepository continueReadingRealtimeRepository;
    private final MonthlyReadingRankHistoryRepository monthlyReadingRankHistoryRepository;
    private final ContinueReadingRankHistoryRepository continueReadingRankHistoryRepository;
    private final ArticleReadHistoryRepository articleReadHistoryRepository;
    private final Clock clock;

    public RankSummaryResponse getRankSummary(Member member, String type) {
        MyPageRankType rankType = MyPageRankType.from(type);
        return RankSummaryResponse.from(List.of(createRankCard(member.getId(), rankType)));
    }

    public RankSummaryResponse getRankSummary(Member member) {
        return RankSummaryResponse.from(List.of(
                createRankCard(member.getId(), MyPageRankType.STREAK),
                createRankCard(member.getId(), MyPageRankType.READING)
        ));
    }

    public CategoryStatsResponse getCategoryStats(Member member, String yearMonthValue) {
        YearMonth yearMonth = parseYearMonth(yearMonthValue);
        LocalDateTime start = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime end = yearMonth.plusMonths(1).atDay(1).atStartOfDay();
        List<CategoryReadCount> categoryReadCounts = articleReadHistoryRepository.countReadsByCategory(
                member.getId(),
                start,
                end
        );
        return CategoryStatsResponse.from(categoryReadCounts);
    }

    private RankCardResponse createRankCard(Long memberId, MyPageRankType rankType) {
        return switch (rankType) {
            case STREAK -> createStreakRankCard(memberId);
            case READING -> createReadingRankCard(memberId);
        };
    }

    private RankCardResponse createStreakRankCard(Long memberId) {
        ContinueReadingRealtime realtime = continueReadingRealtimeRepository.findByMemberId(memberId)
                .orElseThrow(() -> entityNotFound(memberId, "ContinueReadingRealtime"));
        LocalDate currentMonth = LocalDate.now(clock).withDayOfMonth(1);
        List<ContinueReadingRankHistory> histories = continueReadingRankHistoryRepository
                .findRecentBeforePeriodByMemberId(memberId, currentMonth, PageRequest.of(0, RANK_HISTORY_LIMIT))
                .stream()
                .sorted(Comparator.comparing(ContinueReadingRankHistory::getPeriod))
                .toList();
        LocalDate today = LocalDate.now(clock);
        List<RankHistoryResponse> rankHistories = histories.stream()
                .map(history -> RankHistoryResponse.of(
                        history.getPeriod(),
                        history.getRankOrder(),
                        today
                ))
                .toList();

        return RankCardResponse.of(
                MyPageRankType.STREAK.value(),
                findLatestRank(rankHistories),
                rankHistories,
                realtime.getDayCount()
        );
    }

    private RankCardResponse createReadingRankCard(Long memberId) {
        LocalDate currentMonth = LocalDate.now(clock).withDayOfMonth(1);
        List<MonthlyReadingRankHistory> histories = monthlyReadingRankHistoryRepository
                .findRecentBeforePeriodByMemberId(memberId, currentMonth, PageRequest.of(0, RANK_HISTORY_LIMIT))
                .stream()
                .sorted(Comparator.comparing(MonthlyReadingRankHistory::getPeriod))
                .toList();
        LocalDate today = LocalDate.now(clock);
        List<RankHistoryResponse> rankHistories = histories.stream()
                .map(history -> RankHistoryResponse.of(
                        history.getPeriod(),
                        history.getRankOrder(),
                        today
                ))
                .toList();

        return RankCardResponse.of(
                MyPageRankType.READING.value(),
                findLatestRank(rankHistories),
                rankHistories,
                articleReadHistoryRepository.countByMemberId(memberId)
        );
    }

    private Long findLatestRank(List<RankHistoryResponse> rankHistories) {
        if (rankHistories.isEmpty()) {
            return null;
        }
        return rankHistories.getLast().rank();
    }

    private YearMonth parseYearMonth(String yearMonth) {
        if (yearMonth == null || yearMonth.isBlank()) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION)
                    .addContext("yearMonth", yearMonth);
        }
        try {
            return YearMonth.parse(yearMonth);
        } catch (DateTimeParseException e) {
            throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION)
                    .addContext("yearMonth", yearMonth);
        }
    }

    private CIllegalArgumentException entityNotFound(Long memberId, String entityType) {
        return new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                .addContext(ErrorContextKeys.ENTITY_TYPE, entityType);
    }
}
