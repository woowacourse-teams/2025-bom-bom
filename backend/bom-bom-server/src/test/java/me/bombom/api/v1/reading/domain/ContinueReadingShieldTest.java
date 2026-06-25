package me.bombom.api.v1.reading.domain;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import org.junit.jupiter.api.Test;

class ContinueReadingShieldTest {

    @Test
    void 보호막_create는_월간_1개_리워드_0개로_생성한다() {
        // when
        ContinueReadingShield shield = ContinueReadingShield.create(1L);

        // then
        assertSoftly(softly -> {
            softly.assertThat(shield.getMemberId()).isEqualTo(1L);
            softly.assertThat(shield.getMonthlyRemainingCount()).isEqualTo(1);
            softly.assertThat(shield.getRewardRemainingCount()).isZero();
            softly.assertThat(shield.getRemainingCount()).isEqualTo(1);
        });
    }

    @Test
    void 보호막_잔여_개수는_음수일_수_없다() {
        assertThatThrownBy(() -> ContinueReadingShield.builder()
                .memberId(1L)
                .monthlyRemainingCount(-1)
                .rewardRemainingCount(0)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보호막 잔여 개수는 음수일 수 없습니다.");

        assertThatThrownBy(() -> ContinueReadingShield.builder()
                .memberId(1L)
                .monthlyRemainingCount(0)
                .rewardRemainingCount(-1)
                .build())
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("보호막 잔여 개수는 음수일 수 없습니다.");
    }
}
