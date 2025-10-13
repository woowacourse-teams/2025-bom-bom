package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.service.AppleOAuth2Service;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberProfileUpdateRequest;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import me.bombom.support.IntegrationTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@IntegrationTest
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @MockitoBean
    private AppleOAuth2Service appleOAuth2Service;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAllInBatch();
    }

    @Test
    void 회원가입_중_이미_존재하는_닉네임이면_예외_발생() {
        //given
        String duplicateNickname = "nickname";
        Member member = TestFixture.createMemberFixture("email", duplicateNickname);
        memberRepository.save(member);

        //when
        PendingOAuth2Member oAuth2Member = PendingOAuth2Member.builder()
                .provider("provider")
                .providerId("providerId")
                .profileUrl("profileUrl")
                .build();
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest(
                duplicateNickname,
                "email2",
                LocalDate.of(2000, 1, 1),
                Gender.MALE
        );

        //then
        assertThatThrownBy(() -> memberService.signup(oAuth2Member, memberSignupRequest))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATE_NICKNAME);
    }

    @Test
    void 회원가입_중_이미_존재하는_이메일이면_예외_발생() {
        //given
        String duplicateEmail = "email@bombom.news";
        Member member = TestFixture.createMemberFixture(duplicateEmail, "nickname");
        memberRepository.save(member);

        //when
        PendingOAuth2Member oAuth2Member = PendingOAuth2Member.builder()
                .provider("provider2")
                .providerId("providerId2")
                .profileUrl("profileUrl")
                .build();
        MemberSignupRequest memberSignupRequest = new MemberSignupRequest(
                "nickname2",
                duplicateEmail,
                LocalDate.of(2000, 1, 1),
                Gender.MALE
        );

        //then
        assertThatThrownBy(() -> memberService.signup(oAuth2Member, memberSignupRequest))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATE_EMAIL);
    }

    @Test
    void 회원_정보_수정_성공() {
        //given
        Member member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(
                "newNickname",
                "newProfileImageUrl",
                LocalDate.of(2000, 1, 1),
                Gender.FEMALE
        );

        //when
        memberService.updateProfile(member.getId(), request);

        //then
        Member updatedMember = memberRepository.findById(member.getId()).get();
        assertSoftly(softly -> {
            softly.assertThat(updatedMember.getNickname()).isEqualTo(request.nickname());
            softly.assertThat(updatedMember.getProfileImageUrl()).isEqualTo(request.profileImageUrl());
            softly.assertThat(updatedMember.getBirthDate()).isEqualTo(request.birthDate());
            softly.assertThat(updatedMember.getGender()).isEqualTo(request.gender());
        });
    }

    @Test
    void 회원_정보_수정_중_이미_존재하는_닉네임이면_예외_발생() {
        //given
        String nickname = "nickname";
        String duplicateNickname = "duplicateNickname";
        Member member1 = TestFixture.createUniqueMember(nickname, "providerId1");
        memberRepository.save(member1);
        Member member2 = TestFixture.createUniqueMember(duplicateNickname, "providerId2");
        memberRepository.save(member2);
        MemberProfileUpdateRequest request = new MemberProfileUpdateRequest(
                duplicateNickname,
                "newProfileImageUrl",
                LocalDate.of(2000, 1, 1),
                Gender.FEMALE
        );

        //then
        assertThatThrownBy(() -> memberService.updateProfile(member1.getId(), request))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.DUPLICATE_NICKNAME);
    }
}