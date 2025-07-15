package me.bombom.api.v1.member.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.WeeklyGoal;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalRequest;
import me.bombom.api.v1.member.dto.response.WeeklyGoalResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.WeeklyGoalRepository;
import org.springframework.data.crossstore.ChangeSetPersister.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final WeeklyGoalRepository weeklyGoalRepository;

    public WeeklyGoalResponse updateWeeklyGoal(UpdateWeeklyGoalRequest request) {
        Member member = memberRepository.findById(request.memberId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        WeeklyGoal weeklyGoal = weeklyGoalRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        weeklyGoal.updateWeeklyGoalCount(request.weeklyGoalCount());
        return WeeklyGoalResponse.from(weeklyGoal);
    }
}
