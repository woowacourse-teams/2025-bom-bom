package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
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
