package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.Optional;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import({MemberService.class, QuerydslConfig.class})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TestEntityManager entityManager;

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
        String duplicateEmail = "email";
        Member member = TestFixture.createMemberFixture(duplicateEmail, "nickname");
        memberRepository.save(member);

        //when
        PendingOAuth2Member oAuth2Member = PendingOAuth2Member.builder()
                .provider("provider")
                .providerId("providerId")
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
    void 회원_탈퇴_성공() {
        // given
        Member member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        Long memberId = member.getId();

        // when
        memberService.withdraw(memberId);

        entityManager.flush();
        entityManager.clear();

        // then
        Optional<Member> foundMemberOptional = memberRepository.findById(memberId);
        assertThat(foundMemberOptional).isEmpty();
    }

    @Test
    void 존재하지_않는_회원_탈퇴_시_예외_발생() {
        // given
        Long nonExistentMemberId = 0L;

        // when & then
        assertThatThrownBy(() -> memberService.withdraw(nonExistentMemberId))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND)
                .hasFieldOrPropertyWithValue("context.memberId", nonExistentMemberId)
                .hasFieldOrPropertyWithValue("context.entityType", "member");
    }
}
