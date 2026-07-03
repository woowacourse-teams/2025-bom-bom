package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import me.bombom.api.v1.challenge.dto.CompletedChallengeFlat;
import org.junit.jupiter.api.Test;

class CompletedChallengeSummaryTest {

    private static final Long CHALLENGE_ID = 201L;
    private static final String TITLE = "30일 독서 챌린지";
    private static final LocalDate START = LocalDate.of(2024, 4, 10);
    private static final LocalDate END = LocalDate.of(2024, 5, 9);

    @Test
    void 챌린지_정보와_출석률_등급을_계산한다() {
        CompletedChallengeSummary summary = CompletedChallengeSummary.from(challenge(10, 10, true));

        assertSoftly(softly -> {
            softly.assertThat(summary.challengeId()).isEqualTo(CHALLENGE_ID);
            softly.assertThat(summary.title()).isEqualTo(TITLE);
            softly.assertThat(summary.startDate()).isEqualTo(START);
            softly.assertThat(summary.endDate()).isEqualTo(END);
            softly.assertThat(summary.attendanceRate()).isEqualTo(100);
            softly.assertThat(summary.grade()).isEqualTo(ChallengeGrade.GOLD);
        });
    }

    @Test
    void 진행률에_따라_등급이_매핑된다() {
        assertSoftly(softly -> {
            softly.assertThat(CompletedChallengeSummary.from(challenge(10, 10, true)).grade())
                    .isEqualTo(ChallengeGrade.GOLD);
            softly.assertThat(CompletedChallengeSummary.from(challenge(9, 10, true)).grade())
                    .isEqualTo(ChallengeGrade.SILVER);
            softly.assertThat(CompletedChallengeSummary.from(challenge(8, 10, true)).grade())
                    .isEqualTo(ChallengeGrade.BRONZE);
            softly.assertThat(CompletedChallengeSummary.from(challenge(7, 10, true)).grade())
                    .isEqualTo(ChallengeGrade.FAIL);
        });
    }

    @Test
    void 생존하지_못하면_진행률이_높아도_FAIL이고_출석률은_그대로다() {
        CompletedChallengeSummary summary = CompletedChallengeSummary.from(challenge(10, 10, false));

        assertSoftly(softly -> {
            softly.assertThat(summary.grade()).isEqualTo(ChallengeGrade.FAIL);
            softly.assertThat(summary.attendanceRate()).isEqualTo(100);
        });
    }

    @Test
    void totalDays가_0이면_출석률은_0이고_FAIL이다() {
        CompletedChallengeSummary summary = CompletedChallengeSummary.from(challenge(0, 0, true));

        assertSoftly(softly -> {
            softly.assertThat(summary.attendanceRate()).isZero();
            softly.assertThat(summary.grade()).isEqualTo(ChallengeGrade.FAIL);
        });
    }

    private static CompletedChallengeFlat challenge(int completedDays, int totalDays, boolean isSurvived) {
        return new CompletedChallengeFlat(CHALLENGE_ID, TITLE, START, END, completedDays, totalDays, isSurvived);
    }
}
