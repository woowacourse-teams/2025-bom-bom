package me.bombom.api.v1.reading.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.repository.MemberRepository;
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
    public void createReadingInformation(Long memberId) {
        ContinueReading newContinueReading = ContinueReading.create(memberId);
        continueReadingRepository.save(newContinueReading);

        TodayReading newTodayReading = TodayReading.create(memberId);
        todayReadingRepository.save(newTodayReading);

        WeeklyReading newWeeklyReading = WeeklyReading.create(memberId);
        weeklyReadingRepository.save(newWeeklyReading);
    }

    @Transactional
    public void resetContinueReadingCount() {
        todayReadingRepository.findAll()
                .stream()
                .filter(todayReading -> todayReading.getTotalCount() != 0)
                .forEach(todayReading -> {
                    Long memberId = todayReading.getMemberId();
                    ContinueReading continueReading = continueReadingRepository.findById(memberId)
                            .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
                    if (todayReading.getCurrentCount() == 0) {
                        continueReading.resetDayCount();
                    } else {
                        continueReading.increaseDayCount();
                    }
                });
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
    public WeeklyGoalCountResponse updateWeeklyGoalCount(UpdateWeeklyGoalCountRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyReading.updateGoalCount(request.weeklyGoalCount());
        return WeeklyGoalCountResponse.from(weeklyReading);
    }

    @Transactional
    public void updateReadingCount(Article article) {
        updateTodayReadingCount(article);
        updateWeeklyReadingCount(article);
    }

    private void updateTodayReadingCount(Article article) {
        if (article.isArrivedToday()) {
            TodayReading todayReading = todayReadingRepository.findByMemberId(article.getMemberId())
                    .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
            todayReading.increaseCurrentCount();
        }
    }

    private void updateWeeklyReadingCount(Article article) {
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(article.getMemberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyReading.increaseCurrentCount();
    }

    public ReadingInformationResponse getReadingInformation(Member member) {
        Long memberId = member.getId();
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return ReadingInformationResponse.of(continueReading, todayReading, weeklyReading);
    }
}

