package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class ChallengeParticipantDomainTest {

    @Test
    void 진행률은_totalDays가_0_이하면_0이고_100을_초과하지_않는다() {
        ChallengeParticipant participant = participant(12, 0, 0);
        ChallengeParticipant inProgress = participant(5, 0, 0);

        assertSoftly(softly -> {
            softly.assertThat(participant.calculateProgress(0)).isZero();
            softly.assertThat(participant.calculateProgress(10)).isEqualTo(100);
            softly.assertThat(inProgress.calculateProgress(10)).isEqualTo(50);
        });
    }

    @Test
    void 보유한_쉴드가_있으면_쉴드를_사용하고_완료일을_증가시킨다() {
        // given
        ChallengeParticipant participant = participant(3, 2, 4);

        // when
        boolean used = participant.useShieldIfAvailable();

        // then
        assertSoftly(softly -> {
            softly.assertThat(used).isTrue();
            softly.assertThat(participant.getShield()).isEqualTo(1);
            softly.assertThat(participant.getCompletedDays()).isEqualTo(4);
            softly.assertThat(participant.getStreak()).isEqualTo(4);
        });
    }

    @Test
    void 쉴드가_없으면_쉴드_사용에_실패하고_완료일을_변경하지_않는다() {
        // given
        ChallengeParticipant participant = participant(3, 0, 4);

        // when
        boolean used = participant.useShieldIfAvailable();

        // then
        assertSoftly(softly -> {
            softly.assertThat(used).isFalse();
            softly.assertThat(participant.getShield()).isZero();
            softly.assertThat(participant.getCompletedDays()).isEqualTo(3);
        });
    }

    @Test
    void 참여자의_생존과_스트릭_날짜를_갱신한다() {
        // given
        ChallengeParticipant participant = participant(0, 0, 0);
        LocalDate participatedDate = LocalDate.of(2026, 1, 26);

        // when
        participant.applyHolidayShield();
        participant.increaseCompletedDays();
        participant.increaseStreak();
        participant.updateLastParticipatedDate(participatedDate);
        participant.markAsFailed();
        participant.resetStreak();

        // then
        assertSoftly(softly -> {
            softly.assertThat(participant.getCompletedDays()).isEqualTo(2);
            softly.assertThat(participant.isSurvived()).isFalse();
            softly.assertThat(participant.getStreak()).isZero();
            softly.assertThat(participant.getLastParticipatedDate()).isEqualTo(participatedDate);
        });
    }

    private static ChallengeParticipant participant(int completedDays, int shield, int streak) {
        return ChallengeParticipant.builder()
                .challengeId(1L)
                .memberId(1L)
                .completedDays(completedDays)
                .shield(shield)
                .streak(streak)
                .isSurvived(true)
                .build();
    }
}
