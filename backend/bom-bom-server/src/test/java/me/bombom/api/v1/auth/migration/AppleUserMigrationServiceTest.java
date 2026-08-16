package me.bombom.api.v1.auth.migration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AppleUserMigrationServiceTest {

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private AppleUserMigrationClient appleUserMigrationClient;

    @InjectMocks
    private AppleUserMigrationService service;

    @Test
    void 미처리_Apple_회원에게만_transfer_sub를_저장한다() {
        Member member = appleMember(1L, "old-sub");
        given(memberRepository.countByProviderAndAppleTransferSubIsNull("apple")).willReturn(1L);
        given(memberRepository.findFirst100ByProviderAndAppleTransferSubIsNullAndIdGreaterThanOrderByIdAsc("apple", 0L))
                .willReturn(List.of(member));
        given(memberRepository.findFirst100ByProviderAndAppleTransferSubIsNullAndIdGreaterThanOrderByIdAsc("apple", 1L))
                .willReturn(List.of());
        given(appleUserMigrationClient.createTransferSub("old-sub")).willReturn("transfer-sub");

        AppleUserMigrationResult result = service.migrateAll();

        assertSoftly(softly -> {
            softly.assertThat(member.getAppleTransferSub()).isEqualTo("transfer-sub");
            softly.assertThat(result.targetCount()).isEqualTo(1L);
            softly.assertThat(result.migratedCount()).isEqualTo(1L);
        });
        verify(memberRepository).save(member);
    }

    @Test
    void Apple_호출에_실패하면_해당_회원값을_저장하지_않는다() {
        Member member = appleMember(1L, "old-sub");
        given(memberRepository.countByProviderAndAppleTransferSubIsNull("apple")).willReturn(1L);
        given(memberRepository.findFirst100ByProviderAndAppleTransferSubIsNullAndIdGreaterThanOrderByIdAsc("apple", 0L))
                .willReturn(List.of(member));
        given(appleUserMigrationClient.createTransferSub("old-sub")).willThrow(new IllegalStateException("Apple error"));

        assertThatThrownBy(service::migrateAll).isInstanceOf(IllegalStateException.class);

        assertThat(member.getAppleTransferSub()).isNull();
        verify(memberRepository, never()).save(member);
    }

    private Member appleMember(Long id, String providerId) {
        return Member.builder()
                .id(id)
                .provider("apple")
                .providerId(providerId)
                .email(providerId + "@example.com")
                .nickname("member-" + id)
                .gender(Gender.NONE)
                .roleId(1L)
                .build();
    }
}
