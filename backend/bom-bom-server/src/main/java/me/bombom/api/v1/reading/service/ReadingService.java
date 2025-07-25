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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReadingService {

    private final MemberRepository memberRepository;
    private final ContinueReadingRepository continueReadingRepository;
    private final TodayReadingRepository todayReadingRepository;
    private final WeeklyReadingRepository weeklyReadingRepository;

    @Transactional
    public WeeklyGoalCountResponse updateWeeklyGoalCount(UpdateWeeklyGoalCountRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyReading.updateGoalCount(request.weeklyGoalCount());
        return WeeklyGoalCountResponse.from(weeklyReading);
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

    @Transactional
    public void updateReadingCount(Article article){
        updateTodayReadingCount(article);
        updateWeeklyReadingCount(article);
    }

    private void updateTodayReadingCount(Article article) {
        if(article.isArrivedToday()) {
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
}

