package me.bombom.api.v1.reading.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.ContinueReadingRankingScheduleProperties;
import me.bombom.api.v1.common.MonthlyRankingScheduleProperties;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.reading.domain.ContinueReadingRealtime;
import me.bombom.api.v1.reading.domain.ContinueReadingSnapshot;
import me.bombom.api.v1.reading.domain.LowestRankWithDifference;
import me.bombom.api.v1.reading.domain.MonthlyReadingRealtime;
import me.bombom.api.v1.reading.domain.MonthlyReadingSnapshot;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.domain.YearlyReading;
import me.bombom.api.v1.reading.dto.ContinueReadingRankFlat;
import me.bombom.api.v1.reading.dto.MonthlyReadingRankFlat;
import me.bombom.api.v1.reading.dto.RankerInfo;
import me.bombom.api.v1.reading.dto.response.ContinueReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.ContinueReadingRankingResponse;
import me.bombom.api.v1.reading.dto.response.MemberContinueReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingCountResponse;
import me.bombom.api.v1.reading.dto.response.MemberMonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankingResponse;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.badge.service.BadgeService;
import me.bombom.api.v1.reading.repository.ContinueReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.ContinueReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRealtimeRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingSnapshotRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private static final int LAST_MONTH_OFFSET = 1;

    private final MonthlyReadingSnapshotMetaService monthlyReadingSnapshotMetaService;
    private final ContinueReadingSnapshotMetaService continueReadingRankingSnapshotMetaService;

    private final MemberRepository memberRepository;
    private final ContinueReadingRealtimeRepository continueReadingRepository;
    private final ContinueReadingSnapshotRepository continueReadingRankingSnapshotRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final WeeklyReadingRepository weeklyReadingRepository;
    private final MonthlyReadingSnapshotRepository monthlyReadingSnapshotRepository;
    private final MonthlyReadingRealtimeRepository monthlyReadingRealtimeRepository;
    private final YearlyReadingRepository yearlyReadingRepository;

    private final MonthlyRankingScheduleProperties scheduleProps;
    private final ContinueReadingRankingScheduleProperties continueReadingRankingScheduleProperties;
    private final BadgeService badgeService;
    private final Clock clock;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeReadingInformation(Long memberId) {
        ContinueReadingRealtime newContinueReadingRealtime = ContinueReadingRealtime.create(memberId);
        continueReadingRepository.save(newContinueReadingRealtime);

        continueReadingRankingSnapshotRepository.save(
                ContinueReadingSnapshot.create(
                        memberId,
                        0,
                        computeLowestContinueReadingRankOrder()
                )
        );

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
        LocalDate lastMonth = LocalDate.now().minusMonths(LAST_MONTH_OFFSET);
        int targetYear = lastMonth.getYear();
        try {
            // 1. 데이터가 없으면 바로 realtime 초기화
            long snapshotCount = monthlyReadingSnapshotRepository.count();
            if (snapshotCount == 0) {
                monthlyReadingRealtimeRepository.resetAllCurrentCount();
                return;
            }

            // 2. 초기화 전에 랭킹 뱃지 발급
            List<RankerInfo> topRankers = monthlyReadingSnapshotRepository.findTopRankers(BadgeGrade.MAX_RANK_FOR_BADGE);
            badgeService.issueRankingBadges(topRankers, lastMonth);

            // 3. Stream으로 메모리 효율적 처리
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
        ContinueReadingRealtime continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReadingRealtime"));
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
        ContinueReadingRealtime continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReadingRealtime"));
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

    public MonthlyReadingRankingResponse getMonthlyReadingRank(int limit) {
        LocalDate lastMonth = getLastMonth();
        List<MonthlyReadingRankFlat> flatResults = monthlyReadingSnapshotRepository.findMonthlyRanking(
                limit,
                lastMonth.getYear(),
                lastMonth.getMonthValue()
        );
        List<MonthlyReadingRankResponse> monthlyRanking = MonthlyReadingRankResponse.from(flatResults);
        LocalDateTime rankingUpdatedAt = monthlyReadingSnapshotMetaService.getSnapshotAt();
        ZonedDateTime serverNow = getCurrentServerZoneDateTime(scheduleProps.zoneId());
        ZonedDateTime nextRefreshAt = requireNextRankingRefresh(serverNow, scheduleProps.cronExpression());
        return MonthlyReadingRankingResponse.of(
                rankingUpdatedAt,
                nextRefreshAt.toLocalDateTime(),
                serverNow.toLocalDateTime(),
                monthlyRanking
        );
    }

    public MemberMonthlyReadingRankResponse getMemberMonthlyReadingRank(Member member) {
        LocalDate lastMonth = getLastMonth();
        MonthlyReadingRankFlat flat = monthlyReadingSnapshotRepository.findMemberRanking(
                member.getId(),
                lastMonth.getYear(),
                lastMonth.getMonthValue()
        );
        if (flat == null) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "MonthlyReadingSnapshot");
        }
        return MemberMonthlyReadingRankResponse.from(flat);
    }

    public ContinueReadingRankingResponse getContinueReadingRank(int limit) {
        LocalDate lastMonth = getLastMonth();
        List<ContinueReadingRankFlat> flatResults = continueReadingRankingSnapshotRepository.findContinueReadingRanking(
                limit,
                lastMonth.getYear(),
                lastMonth.getMonthValue()
        );
        List<ContinueReadingRankResponse> ranking = ContinueReadingRankResponse.from(flatResults);
        LocalDateTime rankingUpdatedAt = continueReadingRankingSnapshotMetaService.getSnapshotAt();
        ZonedDateTime serverNow = getCurrentServerZoneDateTime(
                continueReadingRankingScheduleProperties.zoneId()
        );
        ZonedDateTime nextRefreshAt = requireNextRankingRefresh(
                serverNow,
                continueReadingRankingScheduleProperties.cronExpression()
        );
        return ContinueReadingRankingResponse.of(
                rankingUpdatedAt,
                nextRefreshAt.toLocalDateTime(),
                serverNow.toLocalDateTime(),
                ranking
        );
    }

    public MemberContinueReadingRankResponse getMemberContinueReadingRank(Member member) {
        LocalDate lastMonth = getLastMonth();
        return continueReadingRankingSnapshotRepository.findMemberContinueReadingRanking(
                member.getId(),
                lastMonth.getYear(),
                lastMonth.getMonthValue()
        ).map(MemberContinueReadingRankResponse::from)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReadingSnapshot"));
    }

    public MemberMonthlyReadingCountResponse getMemberMonthlyReadingCount(Member member) {
        MonthlyReadingRealtime monthlyReadingRealtime = monthlyReadingRealtimeRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MonthlyReadingRealtime")
                        .addContext(ErrorContextKeys.OPERATION, "getMemberMonthlyReadingCount"));
        return MemberMonthlyReadingCountResponse.from(monthlyReadingRealtime.getCurrentCount());
    }

    @Transactional
    public void updateMonthlyRanking() {
        try {
            log.info("Starting monthly ranking update");
            monthlyReadingSnapshotRepository.updateMonthlyRanking();
            monthlyReadingSnapshotMetaService.updateSnapshotAt();
            log.info("Monthly ranking update completed successfully");
        } catch (Exception e) {
            log.error("Failed to update monthly ranking: {}", e.getMessage(), e);
            throw new CIllegalArgumentException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "updateMonthlyRanking");
        }
    }

    @Transactional
    public void updateContinueReadingRankingSnapshot() {
        try {
            log.info("연속 읽기 랭킹 스냅샷 업데이트를 시작합니다.");
            rebuildContinueReadingRankingSnapshot();
            log.info("연속 읽기 랭킹 스냅샷 업데이트가 완료되었습니다.");
        } catch (Exception e) {
            log.error("연속 읽기 랭킹 스냅샷 업데이트에 실패했습니다. message={}", e.getMessage(), e);
            throw new CIllegalArgumentException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.OPERATION, "updateContinueReadingRankingSnapshot");
        }
    }

    // TODO: 실패한 작업부터 재실행 로직 필요
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteAllByMemberId(Long memberId) {
        try {
            continueReadingRepository.deleteByMemberId(memberId);
            continueReadingRankingSnapshotRepository.deleteByMemberId(memberId);
            todayReadingRepository.deleteByMemberId(memberId);
            weeklyReadingRepository.deleteByMemberId(memberId);
            monthlyReadingSnapshotRepository.deleteByMemberId(memberId);
            monthlyReadingRealtimeRepository.deleteByMemberId(memberId);
            yearlyReadingRepository.deleteByMemberId(memberId);
        } catch (Exception e) {
            log.error("회원 읽기 정보 삭제 실패. memberId = {}", memberId, e.getStackTrace());
        }
    }

    private LocalDate getLastMonth() {
        return LocalDate.now(clock).minusMonths(LAST_MONTH_OFFSET);
    }

    private ZonedDateTime getCurrentServerZoneDateTime(ZoneId zoneId) {
        return ZonedDateTime.now(zoneId);
    }

    private ZonedDateTime requireNextRankingRefresh(ZonedDateTime serverNow, CronExpression cronExpression) {
        ZonedDateTime nextRefreshAt = cronExpression.next(serverNow);
        if (nextRefreshAt == null) {
            log.error(
                    "다음 랭킹 갱신 시간을 계산할 수 없습니다. application.yml의 ranking 스케줄 설정을 확인하세요. serverNow={}",
                    serverNow
            );
            throw new IllegalStateException("nextRefreshAt : 다음 랭킹 갱신 시간이 존재하지 않습니다.");
        }
        return nextRefreshAt;
    }

    private void rebuildContinueReadingRankingSnapshot() {
        continueReadingRankingSnapshotRepository.updateContinueReadingRankingSnapshot();
        continueReadingRankingSnapshotMetaService.updateSnapshotAt();
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

    private long computeLowestContinueReadingRankOrder() {
        if (continueReadingRankingSnapshotRepository.count() == 0) {
            return 1L;
        }
        ContinueReadingSnapshot lowestRankContinueReadingSnapshot =
                continueReadingRankingSnapshotRepository.findTopByOrderByRankOrderDesc();
        if (lowestRankContinueReadingSnapshot.getDayCount() == 0) {
            return lowestRankContinueReadingSnapshot.getRankOrder();
        }
        return lowestRankContinueReadingSnapshot.getRankOrder() + 1;
    }

    private void applyResetContinueReadingCount(TodayReading todayReading) {
        Long memberId = todayReading.getMemberId();
        ContinueReadingRealtime continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReadingRealtime")
                    .addContext(ErrorContextKeys.OPERATION, "applyResetContinueReadingCount"));
        continueReading.resetDayCount();
    }

    private void updateContinueReadingCount(Long memberId) {
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "TodayReading")
                    .addContext(ErrorContextKeys.OPERATION, "updateContinueReadingCount"));
        ContinueReadingRealtime continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "ContinueReadingRealtime")
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

    private boolean isBonusApplicable(ContinueReadingRealtime continueReading) {
        return continueReading.getDayCount() >= ScorePolicyConstants.MIN_CONTINUE_READING_COUNT;
    }

    private boolean canIncreaseContinueReadingCount(TodayReading todayReading) {
        return todayReading.getCurrentCount() == 0;
    }
}
