package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import me.bombom.api.v1.challenge.dto.EndedChallengeParticipationFlat;
import org.junit.jupiter.api.Test;

class MyChallengeSummaryTest {

    private static final Long ME = 1L;
    private static final Long MEMBER_A = 2L;
    private static final Long MEMBER_B = 3L;

    @Test
    void 참여한_종료_챌린지가_없는_회원은_모든_값이_0이다() {
        // 데이터에 ME가 존재하지 않음
        List<EndedChallengeParticipationFlat> participations = List.of(
                participation(MEMBER_A, 9, 10, true)
        );

        MyChallengeSummary summary = MyChallengeSummary.of(participations, ME);

        assertThat(summary).isEqualTo(MyChallengeSummary.EMPTY);
    }

    @Test
    void 본인_통계를_계산한다() {
        List<EndedChallengeParticipationFlat> participations = List.of(
                participation(ME, 8, 10, true)
        );

        MyChallengeSummary summary = MyChallengeSummary.of(participations, ME);

        assertSoftly(softly -> {
            softly.assertThat(summary.completedChallengeCount()).isEqualTo(1);
            softly.assertThat(summary.completionRate()).isEqualTo(100);
            softly.assertThat(summary.averageAttendanceRate()).isEqualTo(80);
            softly.assertThat(summary.medalRatio().getBronze()).isEqualTo(100);
        });
    }

    @Test
    void topPercent는_나보다_점수가_높은_회원_비율이다() {
        // 출석률: ME=80, A=90, B=70 → 나보다 높은 회원 1명(A) / 3명 = 33.3%
        // 완료율(생존 기준): ME=100, A=100, B=0(미생존) → 나보다 높은 회원 0명 = 0.0%
        List<EndedChallengeParticipationFlat> participations = List.of(
                participation(ME, 8, 10, true),
                participation(MEMBER_A, 9, 10, true),
                participation(MEMBER_B, 7, 10, false)
        );

        MyChallengeSummary summary = MyChallengeSummary.of(participations, ME);

        assertSoftly(softly -> {
            softly.assertThat(summary.attendanceTopPercent()).isEqualTo(33.3);
            softly.assertThat(summary.completionTopPercent()).isEqualTo(0.0);
        });
    }

    @Test
    void 모집단에_본인만_있으면_topPercent는_0이다() {
        List<EndedChallengeParticipationFlat> participations = List.of(
                participation(ME, 8, 10, true)
        );

        MyChallengeSummary summary = MyChallengeSummary.of(participations, ME);

        assertSoftly(softly -> {
            softly.assertThat(summary.completionTopPercent()).isEqualTo(0.0);
            softly.assertThat(summary.attendanceTopPercent()).isEqualTo(0.0);
        });
    }

    private static EndedChallengeParticipationFlat participation(
            Long memberId,
            int attendedDays,
            int totalDays,
            boolean isSurvived
    ) {
        return new EndedChallengeParticipationFlat(memberId, attendedDays, totalDays, isSurvived);
    }
}
