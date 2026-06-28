package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.challenge.dto.OngoingChallengeParticipantFlat;
import org.junit.jupiter.api.Test;

class OngoingChallengeSummaryTest {

    private static final Long CHALLENGE_ID = 1L;
    private static final String TITLE = "한 달 뉴스레터 읽기 챌린지";
    private static final LocalDate START = LocalDate.of(2026, 6, 1);
    private static final LocalDate END = LocalDate.of(2026, 6, 30);
    private static final int TOTAL_DAYS = 10;
    private static final Long ME = 1L;
    private static final Long TEAM_1 = 10L;
    private static final Long TEAM_2 = 20L;

    @Test
    void 챌린지_요약을_계산한다() {
        // 팀1: 나(80%), A(100%) / 팀2: B(60%), C(40%)
        List<OngoingChallengeParticipantFlat> participants = List.of(
                participant(ME, TEAM_1, 8),
                participant(2L, TEAM_1, 10),
                participant(3L, TEAM_2, 6),
                participant(4L, TEAM_2, 4)
        );
        LocalDate today = END.minusDays(5);

        OngoingChallengeSummary summary = OngoingChallengeSummary.of(participants, ME, today);

        assertSoftly(softly -> {
            softly.assertThat(summary.challengeId()).isEqualTo(CHALLENGE_ID);
            softly.assertThat(summary.title()).isEqualTo(TITLE);
            softly.assertThat(summary.remainingDays()).isEqualTo(5);
            softly.assertThat(summary.progressRate()).isEqualTo(80);
            // 팀1 내 순위: 나(80) 위로 A(100) 1명 → 2등 / 팀 인원 2
            softly.assertThat(summary.myTeamRank().rank()).isEqualTo(2);
            softly.assertThat(summary.myTeamRank().total()).isEqualTo(2);
            // 팀 평균: 팀1=90, 팀2=50 → 우리 팀 1등 / 팀 2개
            softly.assertThat(summary.teamRank().rank()).isEqualTo(1);
            softly.assertThat(summary.teamRank().total()).isEqualTo(2);
            // 전체 참여자 평균 70 → 나(80) +10
            softly.assertThat(summary.myAttendanceComparison().attendanceRate()).isEqualTo(80);
            softly.assertThat(summary.myAttendanceComparison().differencePoint()).isEqualTo(10);
            // 전체 팀 평균 (90+50)/2=70 → 우리 팀(90) +20
            softly.assertThat(summary.teamAttendanceComparison().attendanceRate()).isEqualTo(90);
            softly.assertThat(summary.teamAttendanceComparison().differencePoint()).isEqualTo(20);
        });
    }

    @Test
    void 동점은_같은_등수이고_다음_등수는_인원만큼_건너뛴다() {
        // 팀1: 나(60%), X(80%), Y(80%) → 나보다 높은 2명 → 3등 (2등 아님)
        List<OngoingChallengeParticipantFlat> participants = List.of(
                participant(ME, TEAM_1, 6),
                participant(2L, TEAM_1, 8),
                participant(3L, TEAM_1, 8)
        );

        OngoingChallengeSummary summary = OngoingChallengeSummary.of(participants, ME, END.minusDays(1));

        assertSoftly(softly -> {
            softly.assertThat(summary.myTeamRank().rank()).isEqualTo(3);
            softly.assertThat(summary.myTeamRank().total()).isEqualTo(3);
        });
    }

    @Test
    void 평균보다_낮으면_differencePoint가_음수다() {
        // 팀1: 나(40%) / 팀2: A(100%) → 전체 평균 70, 팀 평균 평균 70
        List<OngoingChallengeParticipantFlat> participants = List.of(
                participant(ME, TEAM_1, 4),
                participant(2L, TEAM_2, 10)
        );

        OngoingChallengeSummary summary = OngoingChallengeSummary.of(participants, ME, END.minusDays(1));

        assertSoftly(softly -> {
            softly.assertThat(summary.myAttendanceComparison().differencePoint()).isEqualTo(-30);
            softly.assertThat(summary.teamAttendanceComparison().differencePoint()).isEqualTo(-30);
        });
    }

    @Test
    void 마지막날이면_remainingDays는_0이다() {
        OngoingChallengeSummary summary = OngoingChallengeSummary.of(
                List.of(participant(ME, TEAM_1, 8)),
                ME,
                END
        );

        assertThat(summary.remainingDays()).isZero();
    }

    private static OngoingChallengeParticipantFlat participant(Long memberId, Long teamId, int completedDays) {
        return new OngoingChallengeParticipantFlat(
                CHALLENGE_ID, TITLE, START, END, TOTAL_DAYS, teamId, memberId, completedDays
        );
    }
}
