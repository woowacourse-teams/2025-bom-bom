package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;
import org.junit.jupiter.api.Test;

class MedalCountsTest {

    @Test
    void 참여_기록을_수료_등급으로_분류해_등급별_개수를_센다() {
        // 100→GOLD, 90→SILVER, 80→BRONZE
        MedalCounts counts = MedalCounts.from(List.of(
                participation(10, 10, true),
                participation(9, 10, true),
                participation(8, 10, true)
        ));

        assertSoftly(softly -> {
            softly.assertThat(counts.getGold()).isEqualTo(1);
            softly.assertThat(counts.getSilver()).isEqualTo(1);
            softly.assertThat(counts.getBronze()).isEqualTo(1);
            softly.assertThat(counts.total()).isEqualTo(3);
        });
    }

    @Test
    void 진행률이_낮거나_생존하지_못한_미수료는_개수에서_제외된다() {
        MedalCounts counts = MedalCounts.from(List.of(
                participation(7, 10, true),     // 진행률 70 → FAIL
                participation(10, 10, false)    // 미생존 → FAIL
        ));

        assertThat(counts).isEqualTo(MedalCounts.ZERO);
    }

    @Test
    void 참여_기록이_없으면_모든_개수가_0이다() {
        MedalCounts counts = MedalCounts.from(List.of());

        assertThat(counts).isEqualTo(MedalCounts.ZERO);
    }

    private static MemberMedalParticipationFlat participation(int attendedDays, int totalDays, boolean isSurvived) {
        return new MemberMedalParticipationFlat(1L, attendedDays, totalDays, isSurvived);
    }
}
