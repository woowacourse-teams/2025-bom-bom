package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.ContinueReading;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.TodayReading;
import me.bombom.api.v1.member.domain.WeeklyReading;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyCurrentCountRequest;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.member.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.member.dto.response.WeeklyCurrentCountResponse;
import me.bombom.api.v1.member.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.member.repository.ContinueReadingRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.TodayReadingRepository;
import me.bombom.api.v1.member.repository.WeeklyReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final WeeklyReadingRepository weeklyReadingRepository;
    private final ContinueReadingRepository continueReadingRepository;
    private final TodayReadingRepository todayReadingRepository;

    public WeeklyGoalCountResponse updateWeeklyGoalCount(UpdateWeeklyGoalCountRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyReading.updateGoalCount(request.weeklyGoalCount());
        return WeeklyGoalCountResponse.from(weeklyReading);
    }

    public ReadingInformationResponse getReadingInformation(Long memberId) {
        ContinueReading continueReading = continueReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        TodayReading todayReading = todayReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyReading weeklyReading = weeklyReadingRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        return ReadingInformationResponse.of(continueReading, todayReading, weeklyReading);
    }
}
