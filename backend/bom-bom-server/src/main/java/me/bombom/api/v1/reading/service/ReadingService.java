
package me.bombom.api.v1.reading.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.LowestRankWithDifference;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.domain.YearlyReading;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingCountResponse;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private static final int LAST_MONTH_OFFSET = 1;

    private final MemberRepository memberRepository;
    private final ContinueReadingRepository continueReadingRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final WeeklyReadingRepository weeklyReadingRepository;
    private final MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;
    private final MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;
    private final YearlyReadingRepository yearlyReadingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeReadingInformation(Long memberId) {
        ContinueReading newContinueReading = ContinueReading.create(memberId);
        continueReadingRepository.save(newContinueReading);

        TodayReading newTodayReading = TodayReading.create(memberId);
        todayReadingRepository.save(newTodayReading);

        WeeklyReading newWeeklyReading = WeeklyReading.create(memberId);
        weeklyReadingRepository.save(newWeeklyReading);

        LowestRankWithDifference lowestRankWithDifference = computeLowestRankWithDifference();
        MonthlyReadingSnapshot newMonthlyReadingSnapshot = MonthlyReadingSnapshot.create(
                memberId,
                lowestRankWithDifference.rank(),
                lowestRankWithDifference.difference()
        );
        monthlyReadingSnapshotRepository.save(newMonthlyReadingSnapshot);

        MonthlyReadingRealtime monthlyReadingRealtime = MonthlyReadingRealtime.create(memberId);
        monthlyReadingRealtimeRepository.save(monthlyReadingRealtime);


        YearlyReading newYearlyReading = YearlyReading.create(memberId, LocalDate.now().getYear());
        yearlyReadingRepository.save(newYearlyReading);
    }

    @Transactional
    public void resetTodayReadingCount() {
        todayReadingRepository.resetCurrentCount();
    }

    @Transactional
    public void resetWeeklyReadingCount() {
        weeklyReadingRepository.resetCurrentCount();
    }

    @Transactional
    public void resetContinueReadingCount() {
        todayReadingRepository.findTotalNonZeroAndCurrentZero()
                .forEach(this::applyResetContinueReadingCount);
    }

    @Transactional
    public void migrateMonthlyCountToYearlyAndReset() {
        int targetYear = LocalDate.now().minusMonths(LAST_MONTH_OFFSET).getYear();
        try {
            // 1. 데이터가 없으면 바로 realtime 초기화
            long snapshotCount = monthlyReadingSnapshotRepository.count();
            if (snapshotCount == 0) {
                monthlyReadingRealtimeRepository.resetAllCurrentCount();
                return;
            }

            // 2. Stream으로 메모리 효율적 처리
            // forEach는 side effect가 있지만, 트랜잭션 내에서 DB 업데이트가 목적이므로 적절함
            monthlyReadingSnapshotRepository.findAll()
                .stream()
                .peek(snapshot -> log.debug("Processing member {}", snapshot.getMemberId()))
                .forEach(snapshot -> {
                    Long memberId = snapshot.getMemberId();
                    int monthlyCount = snapshot.getCurrentCount();

                    try {
                        int updatedRows = yearlyReadingRepository.increaseMonthlyCountToYearly(memberId, monthlyCount, targetYear);
                        if (updatedRows == 0) {
                            YearlyReading yearlyReading = yearlyReadingRepository.findByMemberIdAndReadingYear(memberId, targetYear)
                                    .orElseGet(() -> {
                                        YearlyReading newYearlyReading = YearlyReading.create(memberId, targetYear);
                                        return yearlyReadingRepository.save(newYearlyReading);
                                    });
                            yearlyReading.increaseCurrentCount(monthlyCount);
                        }
                    } catch (Exception e) {
                        log.error("Failed to migrate monthly count for member {}: {}", memberId, e.getMessage(), e);
                        throw new RuntimeException("Migration failed for member " + memberId, e);
                    }
                });
            monthlyReadingSnapshotRepository.resetAllCurrentCount();
            monthlyReadingRealtimeRepository.resetAllCurrentCount();
        } catch (Exception e) {
            log.error("Critical error in monthly migration: {}", e.getMessage(), e);
            throw new CIllegalArgumentException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "migrateMonthlyCountToYearlyAndReset");
        }
    }

    @Transactional
    public WeeklyGoalCountResponse updateWeeklyGoalCount(Long memberId, Integer weeklyGoalCount) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "WeeklyReading"));
        weeklyReading.updateGoalCount(weeklyGoalCount);
        return WeeklyGoalCountResponse.from(weeklyReading);
    }

    public int calculateArticleScore(Long memberId) {
        int score = ScorePolicyConstants.ARTICLE_READING_SCORE;
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReading"));
        if (isBonusApplicable(continueReading)) {
            score += ScorePolicyConstants.CONTINUE_READING_BONUS_SCORE;
        }
        return score;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateReadingCount(Long memberId, boolean isTodayArticle) {
        if (isTodayArticle) {
            updateContinueReadingCount(memberId);
            updateTodayReadingCount(memberId);
            updateWeeklyReadingCount(memberId);
        }
        updateMonthlyReadingCount(memberId);
    }

    @Transactional
    public void updateReadingCountForGuideMail(Long memberId, boolean isRegisterDay) {
        if (isRegisterDay) {
            updateContinueReadingCount(memberId);
            updateTodayReadingCount(memberId);
            updateWeeklyReadingCount(memberId);
        }
        updateMonthlyReadingCount(memberId);
    }

    public ReadingInformationResponse getReadingInformation(Member member) {
        Long memberId = member.getId();
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReading"));
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "TodayReading"));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "WeeklyReading"));
        return ReadingInformationResponse.of(continueReading, todayReading, weeklyReading);
    }

    public List<MonthlyReadingRankResponse> getMonthlyReadingRank(int limit) {
        return monthlyReadingSnapshotRepository.findMonthlyRanking(limit);
    }

    public MemberMonthlyReadingRankResponse getMemberMonthlyReadingRank(Member member) {
        return monthlyReadingSnapshotRepository.findMemberRankAndGap(member.getId());
    }

    @Transactional
    public void updateMonthlyRanking() {
        try {
            log.info("Starting monthly ranking update");
            monthlyReadingSnapshotRepository.updateMonthlyRanking();
            log.info("Monthly ranking update completed successfully");
        } catch (Exception e) {
            log.error("Failed to update monthly ranking: {}", e.getMessage(), e);
            throw new CIllegalArgumentException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "updateMonthlyRanking");
        }
    }

    public MemberMonthlyReadingCountResponse getMemberMonthlyReadingCount(Member member) {
        MonthlyReadingRealtime monthlyReadingRealtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MonthlyReadingRealtime")
                        .addContext(ErrorContextKeys.OPERATION, "getMemberMonthlyReadingCount"));
        return MemberMonthlyReadingCountResponse.from(monthlyReadingRealtime.getCurrentCount());
    }

    // TODO: 실패한 작업부터 재실행 로직 필요
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        try {
            continueReadingRepository.deleteByMemberId(memberId);
            todayReadingRepository.deleteByMemberId(memberId);
            weeklyReadingRepository.deleteByMemberId(memberId);
            monthlyReadingSnapshotRepository.deleteByMemberId(memberId);
            monthlyReadingRealtimeRepository.deleteByMemberId(memberId);
            yearlyReadingRepository.deleteByMemberId(memberId);
        } catch (Exception e){
            log.error("회원 읽기 정보 삭제 실패. memberId = {}", memberId, e.getStackTrace());
        }
    }

    private LowestRankWithDifference computeLowestRankWithDifference() {
        MonthlyReadingSnapshot lowestRankMonthlyReadingSnapshot = monthlyReadingSnapshotRepository.findTopByOrderByRankOrderDesc();
        if (lowestRankMonthlyReadingSnapshot.getCurrentCount() == 0) {
            return LowestRankWithDifference.of(
                    lowestRankMonthlyReadingSnapshot.getRankOrder(),
                    lowestRankMonthlyReadingSnapshot.getNextRankDifference()
            );
        }
        return LowestRankWithDifference.of(
                lowestRankMonthlyReadingSnapshot.getRankOrder() + 1,
                lowestRankMonthlyReadingSnapshot.getCurrentCount()
        );
    }

    private void applyResetContinueReadingCount(TodayReading todayReading) {
        Long memberId = todayReading.getMemberId();
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReading")
                    .addContext(ErrorContextKeys.OPERATION, "applyResetContinueReadingCount"));
        continueReading.resetDayCount();
    }

    private void updateContinueReadingCount(Long memberId) {
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "TodayReading")
                    .addContext(ErrorContextKeys.OPERATION, "updateContinueReadingCount"));
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReading")
                    .addContext(ErrorContextKeys.OPERATION, "updateContinueReadingCount"));
        if (canIncreaseContinueReadingCount(todayReading)) {
            continueReading.increaseDayCount();
        }
    }

    private void updateTodayReadingCount(Long memberId) {
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "TodayReading")
                    .addContext(ErrorContextKeys.OPERATION, "updateTodayReadingCount"));
        todayReading.increaseCurrentCount();
    }

    private void updateWeeklyReadingCount(Long memberId) {
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "WeeklyReading")
                    .addContext(ErrorContextKeys.OPERATION, "updateWeeklyReadingCount"));
        weeklyReading.increaseCurrentCount();
    }

    private void updateMonthlyReadingCount(Long memberId) {
        MonthlyReadingRealtime monthlyReadingRealtime = monthlyReadingRealtimeRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MonthlyReadingRealtime")
                        .addContext(ErrorContextKeys.OPERATION, "updateMonthlyReadingCount"));
        monthlyReadingRealtime.increaseCurrentCount();
    }

    private boolean isBonusApplicable(ContinueReading continueReading) {
        return continueReading.getDayCount() >= ScorePolicyConstants.MIN_CONTINUE_READING_COUNT;
    }

    private boolean canIncreaseContinueReadingCount(TodayReading todayReading) {
        return todayReading.getCurrentCount() == 0;
    }
}
