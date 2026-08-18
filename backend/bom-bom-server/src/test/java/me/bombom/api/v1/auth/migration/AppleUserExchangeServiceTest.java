package me.bombom.api.v1.auth.migration;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppleUserExchangeServiceTest {

    private static final ZoneId ZONE = ZoneId.of("Asia/Seoul");
    private static final Clock FIXED_CLOCK =
            Clock.fixed(Instant.parse("2026-08-18T12:00:00Z"), ZONE);

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AppleUserExchangeClient appleUserExchangeClient;

    private AppleUserExchangeService service;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        service = new AppleUserExchangeService(memberRepository, appleUserExchangeClient, FIXED_CLOCK);
    }

    @Test
    void 미처리_transfer_sub_보유_회원의_providerId를_새_sub로_갱신한다() {
        Member member = appleMember(1L, "old-team-sub", "transfer-sub");
        given(memberRepository.countByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNull("apple"))
                .willReturn(1L);
        given(memberRepository
                .findFirst100ByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNullAndIdGreaterThanOrderByIdAsc(
                        "apple", 0L))
                .willReturn(List.of(member));
        given(memberRepository
                .findFirst100ByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNullAndIdGreaterThanOrderByIdAsc(
                        "apple", 1L))
                .willReturn(List.of());
        given(appleUserExchangeClient.exchangeSub("transfer-sub")).willReturn("new-team-sub");

        AppleUserExchangeResult result = service.exchangeAll();

        assertSoftly(softly -> {
            softly.assertThat(member.getProviderId()).isEqualTo("new-team-sub");
            softly.assertThat(member.getAppleSubMigratedAt())
                    .isEqualTo(LocalDateTime.now(FIXED_CLOCK));
            softly.assertThat(result.targetCount()).isEqualTo(1L);
            softly.assertThat(result.migratedCount()).isEqualTo(1L);
            softly.assertThat(result.failedCount()).isEqualTo(0L);
        });
        verify(memberRepository).save(member);
    }

    @Test
    void Apple_호출에_실패한_회원은_건너뛰고_다음_회원을_계속_처리한다() {
        Member failedMember = appleMember(1L, "old-team-sub-1", "transfer-sub-1");
        Member succeededMember = appleMember(2L, "old-team-sub-2", "transfer-sub-2");
        given(memberRepository.countByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNull("apple"))
                .willReturn(2L);
        given(memberRepository
                .findFirst100ByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNullAndIdGreaterThanOrderByIdAsc(
                        "apple", 0L))
                .willReturn(List.of(failedMember, succeededMember));
        given(memberRepository
                .findFirst100ByProviderAndAppleTransferSubIsNotNullAndAppleSubMigratedAtIsNullAndIdGreaterThanOrderByIdAsc(
                        "apple", 2L))
                .willReturn(List.of());
        given(appleUserExchangeClient.exchangeSub("transfer-sub-1"))
                .willThrow(new IllegalStateException("Apple error"));
        given(appleUserExchangeClient.exchangeSub("transfer-sub-2")).willReturn("new-team-sub-2");

        AppleUserExchangeResult result = service.exchangeAll();

        assertSoftly(softly -> {
            softly.assertThat(failedMember.getProviderId()).isEqualTo("old-team-sub-1");
            softly.assertThat(failedMember.getAppleSubMigratedAt()).isNull();
            softly.assertThat(succeededMember.getProviderId()).isEqualTo("new-team-sub-2");
            softly.assertThat(result.targetCount()).isEqualTo(2L);
            softly.assertThat(result.migratedCount()).isEqualTo(1L);
            softly.assertThat(result.failedCount()).isEqualTo(1L);
        });
        verify(memberRepository, never()).save(failedMember);
        verify(memberRepository).save(succeededMember);
    }

    private Member appleMember(Long id, String providerId, String transferSub) {
        Member member = Member.builder()
                .id(id)
                .provider("apple")
                .providerId(providerId)
                .email(providerId + "@example.com")
                .nickname("member-" + id)
                .gender(Gender.NONE)
                .roleId(1L)
                .build();
        member.updateAppleTransferSub(transferSub);
        return member;
    }
}
