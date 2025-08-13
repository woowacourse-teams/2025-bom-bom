package me.bombom.api.v1.reading.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.reading.domain.ContinueReading;
import me.bombom.api.v1.reading.domain.TodayReading;
import me.bombom.api.v1.reading.domain.WeeklyReading;
import me.bombom.api.v1.reading.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.reading.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.reading.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.reading.repository.ContinueReadingRepository;
import me.bombom.api.v1.reading.repository.TodayReadingRepository;
import me.bombom.api.v1.reading.repository.WeeklyReadingRepository;
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

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void initializeReadingInformation(Long memberId) {
        ContinueReading newContinueReading = ContinueReading.create(memberId);
        continueReadingRepository.save(newContinueReading);

        TodayReading newTodayReading = TodayReading.create(memberId);
        todayReadingRepository.save(newTodayReading);

        WeeklyReading newWeeklyReading = WeeklyReading.create(memberId);
        weeklyReadingRepository.save(newWeeklyReading);
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
    public WeeklyGoalCountResponse updateWeeklyGoalCount(UpdateWeeklyGoalCountRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", request.memberId()));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", member.getId())
                    .addContext("entityType", "WeeklyReading"));
        weeklyReading.updateGoalCount(request.weeklyGoalCount());
        return WeeklyGoalCountResponse.from(weeklyReading);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true)
    public int calculateArticleScore(Long memberId) {
        int score = ScorePolicyConstants.ARTICLE_READING_SCORE;
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "ContinueReading"));
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
    }

    public ReadingInformationResponse getReadingInformation(Member member) {
        Long memberId = member.getId();
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "ContinueReading"));
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "TodayReading"));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "WeeklyReading"));
        return ReadingInformationResponse.of(continueReading, todayReading, weeklyReading);
    }

    private boolean shouldResetContinueReadingCount(TodayReading todayReading) {
        return (todayReading.getTotalCount() != 0) && (todayReading.getCurrentCount() == 0);
    }

    private void applyResetContinueReadingCount(TodayReading todayReading) {
        Long memberId = todayReading.getMemberId();
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "ContinueReading")
                    .addContext("operation", "applyResetContinueReadingCount"));
        continueReading.resetDayCount();
    }

    private void updateContinueReadingCount(Long memberId) {
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "TodayReading")
                    .addContext("operation", "updateContinueReadingCount"));
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "ContinueReading")
                    .addContext("operation", "updateContinueReadingCount"));
        if (canIncreaseContinueReadingCount(todayReading)) {
            continueReading.increaseDayCount();
        }
    }

    private void updateTodayReadingCount(Long memberId) {
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "TodayReading")
                    .addContext("operation", "updateTodayReadingCount"));
        todayReading.increaseCurrentCount();
    }

    private void updateWeeklyReadingCount(Long memberId) {
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("entityType", "WeeklyReading")
                    .addContext("operation", "updateWeeklyReadingCount"));
        weeklyReading.increaseCurrentCount();
    }

    private boolean isBonusApplicable(ContinueReading continueReading) {
        return continueReading.getDayCount() >= ScorePolicyConstants.MIN_CONTINUE_READING_COUNT;
    }

    private boolean canIncreaseContinueReadingCount(TodayReading todayReading) {
        return todayReading.getCurrentCount() == 0;
    }
}

