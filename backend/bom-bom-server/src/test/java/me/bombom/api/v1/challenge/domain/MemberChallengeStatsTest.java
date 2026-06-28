package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat;
import org.junit.jupiter.api.Test;

class MemberChallengeStatsTest {

    @Test
    void 완료수는_생존한_챌린지_수이고_완료율은_생존_나누기_참여_백분율이다() {
        // 참여 4건 중 생존 3건 → 완료 3, 완료율 75%
        MemberChallengeStats stats = MemberChallengeStats.from(aggregate(4, 3, 80.0));

        assertSoftly(softly -> {
            softly.assertThat(stats.getCompletedChallengeCount()).isEqualTo(3);
            softly.assertThat(stats.getCompletionRate()).isEqualTo(75);
        });
    }

    @Test
    void 완료율은_반올림한다() {
        // 생존 1 / 참여 3 → 33.33 → 33
        MemberChallengeStats stats = MemberChallengeStats.from(aggregate(3, 1, 50.0));

        assertThat(stats.getCompletionRate()).isEqualTo(33);
    }

    @Test
    void 평균_출석률은_집계값을_반올림한다() {
        MemberChallengeStats stats = MemberChallengeStats.from(aggregate(2, 2, 89.5));

        assertThat(stats.getAverageAttendanceRate()).isEqualTo(90);
    }

    private static MemberChallengeRankingStatsFlat aggregate(int participated, int survived, double averageAttendance) {
        return new MemberChallengeRankingStatsFlat(1L, (long) participated, (long) survived, averageAttendance);
    }
}
