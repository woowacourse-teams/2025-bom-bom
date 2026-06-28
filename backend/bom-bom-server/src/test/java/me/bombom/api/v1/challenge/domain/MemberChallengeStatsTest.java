package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.challenge.dto.EndedChallengeParticipationFlat;
import org.junit.jupiter.api.Test;

class MemberChallengeStatsTest {

    @Test
    void 참여한_종료_챌린지가_없으면_모든_값이_0이다() {
        MemberChallengeStats stats = MemberChallengeStats.from(List.of());

        assertSoftly(softly -> {
            softly.assertThat(stats.getCompletedChallengeCount()).isZero();
            softly.assertThat(stats.getCompletionRate()).isZero();
            softly.assertThat(stats.getAverageAttendanceRate()).isZero();
            softly.assertThat(stats.getMedalRatio()).isEqualTo(MedalRatio.ZERO);
        });
    }

    @Test
    void 생존하고_진행률_등급이_있으면_완료와_메달에_모두_집계한다() {
        // 100→GOLD, 90→SILVER, 80→BRONZE (모두 생존 + 수료)
        MemberChallengeStats stats = MemberChallengeStats.from(List.of(
                participation(10, 10, true),
                participation(9, 10, true),
                participation(8, 10, true)
        ));

        assertSoftly(softly -> {
            softly.assertThat(stats.getCompletedChallengeCount()).isEqualTo(3);
            softly.assertThat(stats.getCompletionRate()).isEqualTo(100);
            softly.assertThat(stats.getMedalRatio().getGold()).isEqualTo(34);
            softly.assertThat(stats.getMedalRatio().getSilver()).isEqualTo(33);
            softly.assertThat(stats.getMedalRatio().getBronze()).isEqualTo(33);
        });
    }

    @Test
    void 생존했지만_진행률이_낮으면_완료에는_포함되지만_메달에는_포함되지_않는다() {
        // 완료는 생존 기준, 메달은 수료 등급 기준 → 정책 분리 검증
        MemberChallengeStats stats = MemberChallengeStats.from(List.of(
                participation(7, 10, true)
        ));

        assertSoftly(softly -> {
            softly.assertThat(stats.getCompletedChallengeCount()).isEqualTo(1);
            softly.assertThat(stats.getCompletionRate()).isEqualTo(100);
            softly.assertThat(stats.getAverageAttendanceRate()).isEqualTo(70);
            softly.assertThat(stats.getMedalRatio()).isEqualTo(MedalRatio.ZERO);
        });
    }

    @Test
    void 생존하지_못하면_완료에도_메달에도_집계하지_않는다() {
        MemberChallengeStats stats = MemberChallengeStats.from(List.of(
                participation(10, 10, false)
        ));

        assertSoftly(softly -> {
            softly.assertThat(stats.getCompletedChallengeCount()).isZero();
            softly.assertThat(stats.getCompletionRate()).isZero();
            softly.assertThat(stats.getAverageAttendanceRate()).isEqualTo(100);
            softly.assertThat(stats.getMedalRatio()).isEqualTo(MedalRatio.ZERO);
        });
    }

    @Test
    void 완료율은_생존한_챌린지_수를_참여한_챌린지_수로_나눈_백분율이다() {
        // 참여 4건 중 생존 3건 → 75%
        MemberChallengeStats stats = MemberChallengeStats.from(List.of(
                participation(10, 10, true),
                participation(9, 10, true),
                participation(8, 10, true),
                participation(5, 10, false)
        ));

        assertThat(stats.getCompletionRate()).isEqualTo(75);
    }

    @Test
    void 평균_출석률은_챌린지별_진행률의_평균이다() {
        // 진행률 100, 50의 평균 → 75
        MemberChallengeStats stats = MemberChallengeStats.from(List.of(
                participation(10, 10, true),
                participation(5, 10, true)
        ));

        assertThat(stats.getAverageAttendanceRate()).isEqualTo(75);
    }

    private static EndedChallengeParticipationFlat participation(int attendedDays, int totalDays, boolean isSurvived) {
        return new EndedChallengeParticipationFlat(1L, attendedDays, totalDays, isSurvived);
    }
}
