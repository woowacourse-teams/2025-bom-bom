package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.byLessThan;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.domain.WeeklyReading;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyCurrentCountRequest;
import me.bombom.api.v1.member.dto.request.UpdateWeeklyGoalCountRequest;
import me.bombom.api.v1.member.dto.response.ReadingInformationResponse;
import me.bombom.api.v1.member.dto.response.WeeklyGoalCountResponse;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.ContinueReadingRepository;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.api.v1.member.repository.TodayReadingRepository;
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

    @Autowired
    private ContinueReadingRepository continueReadingRepository;

    @Autowired
    private TodayReadingRepository todayReadingRepository;

    @Test
    void 주간_목표를_수정할_수_있다() {
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
    void 읽기_현황_종합_정보를_조회할_수_있다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        continueReadingRepository.save(TestFixture.continueReadingFixture(savedMember));
        todayReadingRepository.save(TestFixture.todayReadingFixture(savedMember));
        weeklyReadingRepository.save(TestFixture.weeklyReadingFixture(savedMember));

        // when
        ReadingInformationResponse response = memberService.getReadingInformation(savedMember.getId());

        // then
        assertSoftly(softly -> {
            softly.assertThat(response.streakReadDay()).isEqualTo(10);
            softly.assertThat(response.today().readCount()).isEqualTo(1);
            softly.assertThat(response.today().totalCount()).isEqualTo(3);
            softly.assertThat(response.weekly().readCount()).isEqualTo(3);
            softly.assertThat(response.weekly().goalCount()).isEqualTo(5);
        });
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_회원_정보가_존재하지_않을_경우_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> memberService.getReadingInformation(1L))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_연속_읽기가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());

        // when & then
        assertThatThrownBy(() -> memberService.getReadingInformation(savedMember.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_일간_읽기_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        continueReadingRepository.save(TestFixture.continueReadingFixture(savedMember));

        // when & then
        assertThatThrownBy(() -> memberService.getReadingInformation(savedMember.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 읽기_현황_종합_정보_조회에서_주간_목표_정보가_존재하지_않을_경우_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());
        continueReadingRepository.save(TestFixture.continueReadingFixture(savedMember));
        todayReadingRepository.save(TestFixture.todayReadingFixture(savedMember));

        // when & then
        assertThatThrownBy(() -> memberService.getReadingInformation(savedMember.getId()))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND);
    }

    @Test
    void 유효한_정보로_회원가입에_성공한다() {
        // given
        PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                .provider("provider")
                .providerId("providerId")
                .profileUrl("profileUrl")
                .build();
        MemberSignupRequest signupRequest = MemberSignupRequest.builder()
                .nickname("nickname")
                .birthDate(LocalDateTime.of(2000, 1, 1, 0, 0))
                .gender(Gender.MALE)
                .build();

        // when
        Member savedMember = memberService.signup(pendingMember, signupRequest);

        // then
        assertSoftly(softly -> {
            softly.assertThat(savedMember).isNotNull();
            softly.assertThat(savedMember.getId()).isNotNull();
            softly.assertThat(savedMember.getNickname()).isEqualTo(signupRequest.nickname());
            softly.assertThat(savedMember.getProvider()).isEqualTo(pendingMember.getProvider());
            softly.assertThat(savedMember.getProviderId()).isEqualTo(pendingMember.getProviderId());
            softly.assertThat(savedMember.getProfileImageUrl()).isEqualTo(pendingMember.getProfileUrl());
            softly.assertThat(savedMember.getGender()).isEqualTo(signupRequest.gender());
            softly.assertThat(savedMember.getBirthDate()).isEqualTo(signupRequest.birthDate());
        });
    }

    @Test
    void 중복된_닉네임으로_회원가입_시도시_예외가_발생한다() {
        // given
        Member savedMember = memberRepository.save(TestFixture.normalMemberFixture());

        PendingOAuth2Member pendingMember = PendingOAuth2Member.builder()
                .provider("provider")
                .providerId("providerId")
                .profileUrl("profileUrl")
                .build();
        MemberSignupRequest signupRequest = MemberSignupRequest.builder()
                .nickname(savedMember.getNickname())
                .birthDate(LocalDateTime.of(2000, 1, 1, 0, 0))
                .gender(Gender.MALE)
                .build();

        // when & then
        assertThatThrownBy(() -> memberService.signup(pendingMember, signupRequest))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATE_NICKNAME);
    }
}
