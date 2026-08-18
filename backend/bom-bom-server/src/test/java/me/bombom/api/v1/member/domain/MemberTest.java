package me.bombom.api.v1.member.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDateTime;
import me.bombom.api.v1.member.enums.Gender;
import org.junit.jupiter.api.Test;

class MemberTest {

    @Test
    void Apple_이전_식별자를_저장한다() {
        Member member = Member.builder()
                .provider("apple")
                .providerId("apple-sub")
                .email("apple@example.com")
                .nickname("apple-member")
                .gender(Gender.NONE)
                .roleId(1L)
                .build();

        member.updateAppleTransferSub("transfer-sub");

        assertThat(member.getAppleTransferSub()).isEqualTo("transfer-sub");
    }

    @Test
    void Apple_새_팀_sub로_교환하면_providerId와_완료_시각이_갱신된다() {
        Member member = Member.builder()
                .provider("apple")
                .providerId("old-team-sub")
                .email("apple@example.com")
                .nickname("apple-member")
                .gender(Gender.NONE)
                .roleId(1L)
                .build();
        LocalDateTime migratedAt = LocalDateTime.of(2026, 8, 18, 12, 0);

        member.updateAppleSubMigration("new-team-sub", migratedAt);

        assertSoftly(softly -> {
            softly.assertThat(member.getProviderId()).isEqualTo("new-team-sub");
            softly.assertThat(member.getAppleSubMigratedAt()).isEqualTo(migratedAt);
        });
    }
}
