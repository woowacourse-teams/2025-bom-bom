package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.WeeklyReading;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyCurrentCountRequest;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.member.dto.response.WeeklyCurrentCountResponse;
import me.bombom.api.v1.member.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.WeeklyReadingRepository;
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
    private WeeklyReadingRepository weeklyReadingRepository;

    @Test
    void 주간_목표를_수정할_수_있다(){
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());

        WeeklyReading weeklyReading = WeeklyReading.builder()
                .memberId(savedMember.getId())
                .goalCount(0)
                .currentCount(2)
                .build();
        weeklyReadingRepository.save(weeklyReading);

        UpdateWeeklyGoalCountRequest request = new UpdateWeeklyGoalCountRequest(savedMember.getId(), 3);

        // when
        WeeklyGoalCountResponse result = memberService.updateWeeklyGoalCount(request);

        // then
        assertThat(result.weeklyGoalCount()).isEqualTo(3);
    }

    @Test
    void 주간_목표_수정에서_회원_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        WeeklyReading weeklyReading = WeeklyReading.builder()
                .memberId(0L)
                .goalCount(0)
                .currentCount(2)
                .build();
        weeklyReadingRepository.save(weeklyReading);

        UpdateWeeklyGoalCountRequest request = new UpdateWeeklyGoalCountRequest(0L, 3);

        // when & then
        assertThatThrownBy(() -> memberService.updateWeeklyGoalCount(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 주간_목표_수정에서_주간_목표_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        UpdateWeeklyGoalCountRequest request = new UpdateWeeklyGoalCountRequest(savedMember.getId(), 3);

        // when & then
        assertThatThrownBy(() -> memberService.updateWeeklyGoalCount(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }
    
    @Test
    void 이번_주에_읽은_아티클_수를_갱신할_수_있다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());

        WeeklyReading weeklyReading = WeeklyReading.builder()
                .memberId(savedMember.getId())
                .goalCount(0)
                .currentCount(2)
                .build();
        weeklyReadingRepository.save(weeklyReading);

        UpdateWeeklyCurrentCountRequest request = new UpdateWeeklyCurrentCountRequest(savedMember.getId());
        
        // when
        WeeklyCurrentCountResponse result = memberService.updateWeeklyCurrentCount(request);

        // then
        assertThat(result.currentCount()).isEqualTo(3);
    }

    @Test
    void 이번주_읽은_수_갱신에서_회원_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        WeeklyReading weeklyReading = WeeklyReading.builder()
                .memberId(0L)
                .goalCount(0)
                .currentCount(2)
                .build();
        weeklyReadingRepository.save(weeklyReading);

        UpdateWeeklyCurrentCountRequest request = new UpdateWeeklyCurrentCountRequest(0L);

        // when & then
        assertThatThrownBy(() -> memberService.updateWeeklyCurrentCount(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 이번주_읽은_수_갱신에서_주간_목표_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        UpdateWeeklyCurrentCountRequest request = new UpdateWeeklyCurrentCountRequest(savedMember.getId());

        // when & then
        assertThatThrownBy(() -> memberService.updateWeeklyCurrentCount(request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }
}
