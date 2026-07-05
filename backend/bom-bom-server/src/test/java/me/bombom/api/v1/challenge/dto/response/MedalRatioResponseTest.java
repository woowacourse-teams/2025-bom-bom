package me.bombom.api.v1.challenge.dto.response;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.ArrayList;
import java.util.List;
import me.bombom.api.v1.challenge.domain.MedalCounts;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;
import me.bombom.api.v1.challenge.dto.response.MyChallengeSummaryResponse.MedalRatioResponse;
import org.junit.jupiter.api.Test;

class MedalRatioResponseTest {

    @Test
    void 등급별_개수를_비율로_변환한다() {
        MedalRatioResponse ratio = MedalRatioResponse.from(medalCounts(4, 4, 2));

        assertSoftly(softly -> {
            softly.assertThat(ratio.gold()).isEqualTo(40);
            softly.assertThat(ratio.silver()).isEqualTo(40);
            softly.assertThat(ratio.bronze()).isEqualTo(20);
        });
    }

    @Test
    void 소수점은_버림하므로_합계가_100_미만일_수_있다() {
        // 1/1/1 → 각 33.33 버림 33, 합 99 (남는 1%는 클라이언트가 배분)
        MedalRatioResponse ratio = MedalRatioResponse.from(medalCounts(1, 1, 1));

        assertSoftly(softly -> {
            softly.assertThat(ratio.gold()).isEqualTo(33);
            softly.assertThat(ratio.silver()).isEqualTo(33);
            softly.assertThat(ratio.bronze()).isEqualTo(33);
            softly.assertThat(ratio.gold() + ratio.silver() + ratio.bronze()).isEqualTo(99);
        });
    }

    @Test
    void 수료한_챌린지가_없으면_모든_비율이_0이다() {
        MedalRatioResponse ratio = MedalRatioResponse.from(MedalCounts.ZERO);

        assertSoftly(softly -> {
            softly.assertThat(ratio.gold()).isZero();
            softly.assertThat(ratio.silver()).isZero();
            softly.assertThat(ratio.bronze()).isZero();
        });
    }

    private static MedalCounts medalCounts(int gold, int silver, int bronze) {
        List<MemberMedalParticipationFlat> participations = new ArrayList<>();
        addAll(participations, gold, 10);    // 진행률 100 → GOLD
        addAll(participations, silver, 9);   // 진행률 90 → SILVER
        addAll(participations, bronze, 8);   // 진행률 80 → BRONZE
        return MedalCounts.from(participations);
    }

    private static void addAll(List<MemberMedalParticipationFlat> participations, int count, int attendedDays) {
        for (int i = 0; i < count; i++) {
            participations.add(new MemberMedalParticipationFlat(1L, attendedDays, 10, true));
        }
    }
}
