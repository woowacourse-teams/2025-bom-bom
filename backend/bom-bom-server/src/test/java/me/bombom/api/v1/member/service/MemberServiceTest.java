package me.bombom.api.v1.member.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Optional;
import me.bombom.api.v1.TestFixture;
import me.bombom.api.v1.auth.client.AppleRevokeClient;
import me.bombom.api.v1.auth.dto.PendingOAuth2Member;
import me.bombom.api.v1.auth.enums.OAuth2ProviderInfo;
import me.bombom.api.v1.auth.provider.OAuth2Provider;
import me.bombom.api.v1.auth.provider.OAuth2ProviderFactory;
import me.bombom.api.v1.common.config.QuerydslConfig;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.dto.request.MemberSignupRequest;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@DataJpaTest
@ActiveProfiles("test")
@Import({MemberService.class, QuerydslConfig.class})
class MemberServiceTest {

    @Autowired
    private MemberService memberService;

    @MockitoBean
    private AppleRevokeClient appleRevokeClient;

    @MockitoBean
    private OAuth2ProviderFactory oAuth2ProviderFactory;

    @MockitoBean
    private ApplicationEventPublisher applicationEventPublisher;

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

        OAuth2Provider mockProvider = org.mockito.Mockito.mock(OAuth2Provider.class);
        when(oAuth2ProviderFactory.getProvider(OAuth2ProviderInfo.APPLE)).thenReturn(mockProvider);
        doNothing().when(mockProvider).processWithdrawal(member);

        // when
        memberService.revoke(memberId);

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
        assertThatThrownBy(() -> memberService.revoke(nonExistentMemberId))
                .isInstanceOf(CIllegalArgumentException.class)
                .hasFieldOrPropertyWithValue("errorDetail", ErrorDetail.ENTITY_NOT_FOUND)
                .hasFieldOrPropertyWithValue("context.memberId", nonExistentMemberId)
                .hasFieldOrPropertyWithValue("context.entityType", "member");
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    void 회원_탈퇴_성공_응답_확인() {
        // given
        Member member = TestFixture.normalMemberFixture();
        memberRepository.save(member);
        Long memberId = member.getId();

        OAuth2Provider mockProvider = org.mockito.Mockito.mock(OAuth2Provider.class);
        when(oAuth2ProviderFactory.getProvider(OAuth2ProviderInfo.APPLE)).thenReturn(mockProvider);
        doNothing().when(mockProvider).processWithdrawal(member);

        // when & then - 예외가 발생하지 않으면 성공
        memberService.revoke(memberId);
        
        // 회원이 삭제되었는지 확인
        Optional<Member> foundMember = memberRepository.findById(memberId);
        assertThat(foundMember).isEmpty();
    }
}
