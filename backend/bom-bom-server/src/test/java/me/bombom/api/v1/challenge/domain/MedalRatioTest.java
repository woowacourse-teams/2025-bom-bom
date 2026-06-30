package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;
import org.junit.jupiter.api.Test;

class MedalRatioTest {

    @Test
    void 참여_기록을_수료_등급으로_분류해_메달_비율을_계산한다() {
        // 100→GOLD, 90→SILVER, 80→BRONZE → of(1,1,1)
        MedalRatio ratio = MedalRatio.from(List.of(
                participation(10, 10, true),
                participation(9, 10, true),
                participation(8, 10, true)
        ));

        assertSoftly(softly -> {
            softly.assertThat(ratio.getGold()).isEqualTo(33);
            softly.assertThat(ratio.getSilver()).isEqualTo(33);
            softly.assertThat(ratio.getBronze()).isEqualTo(33);
        });
    }

    @Test
    void 진행률이_낮거나_생존하지_못한_미수료는_메달에서_제외된다() {
        MedalRatio ratio = MedalRatio.from(List.of(
                participation(7, 10, true),     // 진행률 70 → FAIL
                participation(10, 10, false)    // 미생존 → FAIL
        ));

        assertThat(ratio).isEqualTo(MedalRatio.ZERO);
    }

    private static MemberMedalParticipationFlat participation(int attendedDays, int totalDays, boolean isSurvived) {
        return new MemberMedalParticipationFlat(1L, attendedDays, totalDays, isSurvived);
    }

    @Test
    void 수료한_챌린지가_없으면_모든_비율이_0이다() {
        MedalRatio ratio = MedalRatio.of(0, 0, 0);

        assertSoftly(softly -> {
            softly.assertThat(ratio.getGold()).isZero();
            softly.assertThat(ratio.getSilver()).isZero();
            softly.assertThat(ratio.getBronze()).isZero();
        });
    }

    @Test
    void 한_등급만_있으면_해당_등급이_100이다() {
        MedalRatio ratio = MedalRatio.of(3, 0, 0);

        assertSoftly(softly -> {
            softly.assertThat(ratio.getGold()).isEqualTo(100);
            softly.assertThat(ratio.getSilver()).isZero();
            softly.assertThat(ratio.getBronze()).isZero();
        });
    }

    @Test
    void 등급별_개수에_비례해_비율을_계산한다() {
        MedalRatio ratio = MedalRatio.of(4, 4, 2);

        assertSoftly(softly -> {
            softly.assertThat(ratio.getGold()).isEqualTo(40);
            softly.assertThat(ratio.getSilver()).isEqualTo(40);
            softly.assertThat(ratio.getBronze()).isEqualTo(20);
        });
    }

    @Test
    void 소수점은_버림하므로_합계가_100_미만일_수_있다() {
        // 1/1/1 → 각 33.33 버림 33, 합 99 (남는 1%는 클라이언트가 배분)
        MedalRatio ratio = MedalRatio.of(1, 1, 1);

        assertSoftly(softly -> {
            softly.assertThat(ratio.getGold()).isEqualTo(33);
            softly.assertThat(ratio.getSilver()).isEqualTo(33);
            softly.assertThat(ratio.getBronze()).isEqualTo(33);
            softly.assertThat(sum(ratio)).isEqualTo(99);
        });
    }

    @Test
    void 버림_처리로_합계가_100을_넘지_않는다() {
        assertSoftly(softly -> {
            softly.assertThat(sum(MedalRatio.of(1, 2, 4))).isLessThanOrEqualTo(100);
            softly.assertThat(sum(MedalRatio.of(7, 1, 1))).isLessThanOrEqualTo(100);
            softly.assertThat(sum(MedalRatio.of(2, 3, 1))).isLessThanOrEqualTo(100);
        });
    }

    private static int sum(MedalRatio ratio) {
        return ratio.getGold() + ratio.getSilver() + ratio.getBronze();
    }
}
