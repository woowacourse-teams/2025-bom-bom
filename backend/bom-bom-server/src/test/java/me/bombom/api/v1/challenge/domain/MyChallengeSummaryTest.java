package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.challenge.dto.MemberMedalParticipationFlat;
import me.bombom.api.v1.challenge.dto.MemberChallengeRankingStatsFlat;
import org.junit.jupiter.api.Test;

class MyChallengeSummaryTest {

    private static final Long ME = 1L;
    private static final Long MEMBER_A = 2L;
    private static final Long MEMBER_B = 3L;

    @Test
    void 집계에_본인이_없으면_모든_값이_0이다() {
        List<MemberChallengeRankingStatsFlat> aggregates = List.of(aggregate(MEMBER_A, 1, 1, 90.0));

        MyChallengeSummary summary = MyChallengeSummary.of(aggregates, List.of(), ME);

        assertThat(summary).isEqualTo(MyChallengeSummary.EMPTY);
    }

    @Test
    void 본인_통계를_계산한다() {
        List<MemberChallengeRankingStatsFlat> aggregates = List.of(aggregate(ME, 2, 2, 90.0));
        List<MemberMedalParticipationFlat> myParticipations = List.of(
                participation(10, 10, true),
                participation(8, 10, true)
        );

        MyChallengeSummary summary = MyChallengeSummary.of(aggregates, myParticipations, ME);

        assertSoftly(softly -> {
            softly.assertThat(summary.completedChallengeCount()).isEqualTo(2);
            softly.assertThat(summary.completionRate()).isEqualTo(100);
            softly.assertThat(summary.averageAttendanceRate()).isEqualTo(90);
            // 메달은 "개수"로 보유(GOLD 1, BRONZE 1). 비율(%) 변환은 응답 DTO 책임
            softly.assertThat(summary.medalCounts().getGold()).isEqualTo(1);
            softly.assertThat(summary.medalCounts().getBronze()).isEqualTo(1);
        });
    }

    @Test
    void topPercent는_나보다_점수가_높은_회원_비율이다() {
        // 완료율: ME=100, A=100, B=0 → 나보다 높은 회원 0명 = 0.0%
        // 출석률: ME=90, A=95, B=70 → 나보다 높은 회원 1명(A) / 3명 = 33.3%
        List<MemberChallengeRankingStatsFlat> aggregates = List.of(
                aggregate(ME, 2, 2, 90.0),
                aggregate(MEMBER_A, 2, 2, 95.0),
                aggregate(MEMBER_B, 2, 0, 70.0)
        );

        MyChallengeSummary summary = MyChallengeSummary.of(aggregates, List.of(), ME);

        assertSoftly(softly -> {
            softly.assertThat(summary.completionTopPercent()).isEqualTo(0.0);
            softly.assertThat(summary.attendanceTopPercent()).isEqualTo(33.3);
        });
    }

    @Test
    void 모집단에_본인만_있으면_topPercent는_0이다() {
        List<MemberChallengeRankingStatsFlat> aggregates = List.of(aggregate(ME, 2, 2, 90.0));

        MyChallengeSummary summary = MyChallengeSummary.of(aggregates, List.of(), ME);

        assertSoftly(softly -> {
            softly.assertThat(summary.completionTopPercent()).isEqualTo(0.0);
            softly.assertThat(summary.attendanceTopPercent()).isEqualTo(0.0);
        });
    }

    private static MemberChallengeRankingStatsFlat aggregate(
            Long memberId,
            int participated,
            int survived,
            double averageAttendance
    ) {
        return new MemberChallengeRankingStatsFlat(memberId, (long) participated, (long) survived, averageAttendance);
    }

    private static MemberMedalParticipationFlat participation(int attendedDays, int totalDays, boolean isSurvived) {
        return new MemberMedalParticipationFlat(ME, attendedDays, totalDays, isSurvived);
    }
}
