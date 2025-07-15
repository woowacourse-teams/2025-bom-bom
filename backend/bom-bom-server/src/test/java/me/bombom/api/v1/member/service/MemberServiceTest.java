package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.WeeklyGoal;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalRequest;
import me.bombom.api.v1.member.dto.response.WeeklyGoalResponse;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.WeeklyGoalRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(MemberService.class)
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private WeeklyGoalRepository weeklyGoalRepository;

    @Test
    void 주간_목표를_수정할_수_있다(){
        // given
        Member member = Member.builder()
                .email("email")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        memberRepository.save(member);

        WeeklyGoal weeklyGoal = WeeklyGoal.builder()
                .memberId(member.getId())
                .weeklyGoalCount(0)
                .currentCount(2)
                .build();
        weeklyGoalRepository.save(weeklyGoal);

        UpdateWeeklyGoalRequest request = new UpdateWeeklyGoalRequest(member.getId(), 3);

        // when
        WeeklyGoalResponse result = memberService.updateWeeklyGoal(request);

        // then
        assertThat(result.weeklyGoalCount()).isEqualTo(3);
    }

    @Test
    void 회원_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        WeeklyGoal weeklyGoal = WeeklyGoal.builder()
                .memberId(0L)
                .weeklyGoalCount(0)
                .currentCount(2)
                .build();
        weeklyGoalRepository.save(weeklyGoal);

        UpdateWeeklyGoalRequest request = new UpdateWeeklyGoalRequest(0L, 3);

        // when & then
        assertThatThrownBy(() -> memberService.updateWeeklyGoal(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 주간_목표_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member member = Member.builder()
                .email("email")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        memberRepository.save(member);

        UpdateWeeklyGoalRequest request = new UpdateWeeklyGoalRequest(member.getId(), 3);

        // when & then
        assertThatThrownBy(() -> memberService.updateWeeklyGoal(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }
}
