package me.bombom.api.v1.reading.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.MonthlyReading;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.domain.YearlyReading;
import me.bombom.api.v1.reading.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.reading.dto.response.MonthlyReadingRankResponse;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.MonthlyReadingRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
import me.bombom.api.v1.reading.repository.YearlyReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private final MemberRepository memberRepository;
    private final ContinueReadingRepository continueReadingRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final WeeklyReadingRepository weeklyReadingRepository;
    private final MonthlyReadingRepository monthlyReadingRepository;
    private final YearlyReadingRepository yearlyReadingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeReadingInformation(Long memberId) {
        ContinueReading newContinueReading = ContinueReading.create(memberId);
        continueReadingRepository.save(newContinueReading);

        TodayReading newTodayReading = TodayReading.create(memberId);
        todayReadingRepository.save(newTodayReading);

        WeeklyReading newWeeklyReading = WeeklyReading.create(memberId);
        weeklyReadingRepository.save(newWeeklyReading);

        MonthlyReading newMonthlyReading = MonthlyReading.create(memberId);
        monthlyReadingRepository.save(newMonthlyReading);

        YearlyReading newYearlyReading = YearlyReading.create(memberId, LocalDate.now().getYear());
        yearlyReadingRepository.save(newYearlyReading);
    }

    @Transactional
    public void resetTodayReadingCount() {
        todayReadingRepository.findAll()
                .forEach(TodayReading::resetCount);
    }

    @Transactional
    public void resetWeeklyReadingCount() {
        weeklyReadingRepository.findAll()
                .forEach(WeeklyReading::resetCurrentCount);
    }

    @Transactional
    public void resetContinueReadingCount() {
        todayReadingRepository.findAll()
                .stream()
                .filter(this::shouldResetContinueReadingCount)
                .forEach(this::applyResetContinueReadingCount);
    }

    @Transactional
    public void passMonthlyCountToYearly() {
        monthlyReadingRepository.findAll().forEach(monthlyReading -> {
            Long memberId = monthlyReading.getMemberId();
            int targetYear = LocalDate.now().minusMonths(1).getYear();
            YearlyReading yearlyReading = yearlyReadingRepository.findByMemberIdAndReadingYear(memberId, targetYear)
                    .orElseGet(() -> {
                        YearlyReading newYearlyReading = YearlyReading.create(memberId, targetYear);
                        return yearlyReadingRepository.save(newYearlyReading);
                    });
            yearlyReading.increaseCurrentCount(monthlyReading.getCurrentCount());
            monthlyReading.resetCurrentCount();
        });
    }

    @Transactional
    public WeeklyGoalCountResponse updateWeeklyGoalCount(UpdateWeeklyGoalCountRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, request.memberId()));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "WeeklyReading"));
        weeklyReading.updateGoalCount(request.weeklyGoalCount());
        return WeeklyGoalCountResponse.from(weeklyReading);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
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
        // TODO: 규칙 확정 후 연속 읽기 로직 수정
        if (isTodayArticle) {
            updateContinueReadingCount(memberId);
            updateTodayReadingCount(memberId);
        }
        updateWeeklyReadingCount(memberId);
        updateMonthlyReadingCount(memberId);
    }

    @Transactional
    public void updateReadingCountForGuideMail(Long memberId) {
        // TODO: 규칙 확정 후 연속 읽기 로직 수정
        updateContinueReadingCount(memberId);
        updateTodayReadingCount(memberId);
        updateWeeklyReadingCount(memberId);
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
        return monthlyReadingRepository.findRankWithMember(limit);
    }

    private boolean shouldResetContinueReadingCount(TodayReading todayReading) {
        return (todayReading.getTotalCount() != 0) && (todayReading.getCurrentCount() == 0);
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
        MonthlyReading monthlyReading = monthlyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "MonthlyReading")
                        .addContext(ErrorContextKeys.OPERATION, "updateMonthlyReadingCount"));
        monthlyReading.increaseCurrentCount();
    }

    private boolean isBonusApplicable(ContinueReading continueReading) {
        return continueReading.getDayCount() >= ScorePolicyConstants.MIN_CONTINUE_READING_COUNT;
    }

    private boolean canIncreaseContinueReadingCount(TodayReading todayReading) {
        return todayReading.getCurrentCount() == 0;
    }
}

