package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import me.bombom.api.v1.badge.domain.BadgeGrade;
import me.bombom.api.v1.challenge.domain.notification.ChallengeStartNotification;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderNotification;
import me.bombom.api.v1.challenge.domain.notification.ChallengeTodoReminderPhase;
import me.bombom.api.v1.challenge.domain.notification.NotificationStatus;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import org.junit.jupiter.api.Test;

class ChallengePolicyDomainTest {

    @Test
    void ChallengeGrade는_생존여부와_진행률로_등급과_뱃지를_계산한다() {
        assertSoftly(softly -> {
            softly.assertThat(ChallengeGrade.calculate(100, true)).isEqualTo(ChallengeGrade.GOLD);
            softly.assertThat(ChallengeGrade.calculate(90, true)).isEqualTo(ChallengeGrade.SILVER);
            softly.assertThat(ChallengeGrade.calculate(80, true)).isEqualTo(ChallengeGrade.BRONZE);
            softly.assertThat(ChallengeGrade.calculate(79, true)).isEqualTo(ChallengeGrade.FAIL);
            softly.assertThat(ChallengeGrade.calculate(100, false)).isEqualTo(ChallengeGrade.FAIL);
            softly.assertThat(ChallengeGrade.GOLD.toBadge()).contains(BadgeGrade.GOLD);
            softly.assertThat(ChallengeGrade.FAIL.toBadge()).isEmpty();
        });
    }

    @Test
    void ChallengeDailyResult는_쉴드_적용_상태를_판별한다() {
        LocalDate today = LocalDate.of(2026, 1, 26);

        assertSoftly(softly -> {
            softly.assertThat(result(ChallengeDailyStatus.COMPLETE, today).isShieldApplied()).isFalse();
            softly.assertThat(result(ChallengeDailyStatus.SHIELD, today).isShieldApplied()).isTrue();
            softly.assertThat(result(ChallengeDailyStatus.HOLIDAY_SHIELD, today).isShieldApplied()).isTrue();
        });
    }

    @Test
    void ChallengeTodoStatus는_완료여부를_상태로_변환한다() {
        assertThat(ChallengeTodoStatus.getStatus(true)).isEqualTo(ChallengeTodoStatus.COMPLETE);
        assertThat(ChallengeTodoStatus.getStatus(false)).isEqualTo(ChallengeTodoStatus.INCOMPLETE);
    }

    @Test
    void ChallengeTeam은_달성률을_갱신한다() {
        ChallengeTeam team = ChallengeTeam.builder()
                .challengeId(1L)
                .progress(30)
                .build();

        team.updateProgress(80);

        assertThat(team.getProgress()).isEqualTo(80);
    }

    @Test
    void ChallengeFilter_SUMMARY는_참여중인_진행_챌린지_지각모집_사전모집_순으로_정렬한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 10);
        Challenge ongoingJoined = challenge(1L, today.minusDays(1), today.plusDays(10), 10);
        Challenge late = challenge(2L, today.minusDays(1), today.plusDays(10), 30);
        Challenge early = challenge(3L, today.plusDays(1), today.plusDays(10), 10);
        Challenge closed = challenge(4L, today.minusDays(10), today.plusDays(10), 10);

        // when
        Comparator<Challenge> comparator = ChallengeFilter.SUMMARY.orderComparator(today, Set.of(1L)).orElseThrow();
        List<Challenge> sorted = List.of(closed, early, late, ongoingJoined).stream()
                .sorted(comparator)
                .toList();

        // then
        assertThat(sorted)
                .extracting(Challenge::getId)
                .containsExactly(1L, 2L, 3L, 4L);
    }

    @Test
    void ChallengeFilter_DEFAULT는_공개_대상과_비공개_대상을_판별한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 10);
        Challenge comingSoon = challenge(1L, null, null, 10);
        Challenge beforeStart = challenge(2L, today.plusDays(1), today.plusDays(10), 10);
        Challenge latePhase = challenge(3L, today.minusDays(1), today.plusDays(10), 30);
        Challenge closed = challenge(4L, today.minusDays(10), today.plusDays(10), 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(ChallengeFilter.DEFAULT.isVisible(comingSoon, today, false)).isTrue();
            softly.assertThat(ChallengeFilter.DEFAULT.isVisible(beforeStart, today, false)).isTrue();
            softly.assertThat(ChallengeFilter.DEFAULT.isVisible(latePhase, today, false)).isTrue();
            softly.assertThat(ChallengeFilter.DEFAULT.isVisible(closed, today, true)).isTrue();
            softly.assertThat(ChallengeFilter.DEFAULT.isVisible(closed, today, false)).isFalse();
            softly.assertThat(ChallengeFilter.DEFAULT.orderComparator(today, Set.of())).isEmpty();
        });
    }

    @Test
    void ChallengeFilter_SUMMARY는_참여중이거나_모집중인_챌린지만_노출한다() {
        // given
        LocalDate today = LocalDate.of(2026, 1, 10);
        Challenge joinedOngoing = challenge(1L, today.minusDays(1), today.plusDays(10), 10);
        Challenge notJoinedOngoing = challenge(2L, today.minusDays(1), today.plusDays(10), 10);
        Challenge early = challenge(3L, today.plusDays(1), today.plusDays(10), 10);
        Challenge closed = challenge(4L, today.minusDays(10), today.plusDays(10), 10);

        // then
        assertSoftly(softly -> {
            softly.assertThat(ChallengeFilter.SUMMARY.isVisible(joinedOngoing, today, true)).isTrue();
            softly.assertThat(ChallengeFilter.SUMMARY.isVisible(notJoinedOngoing, today, false)).isTrue();
            softly.assertThat(ChallengeFilter.SUMMARY.isVisible(early, today, false)).isTrue();
            softly.assertThat(ChallengeFilter.SUMMARY.isVisible(closed, today, false)).isFalse();
        });
    }

    @Test
    void RegistrationPhase는_사전모집과_지각모집만_모집중으로_판별한다() {
        assertSoftly(softly -> {
            softly.assertThat(RegistrationPhase.EARLY.isRecruiting()).isTrue();
            softly.assertThat(RegistrationPhase.LATE.isRecruiting()).isTrue();
            softly.assertThat(RegistrationPhase.CLOSED.isRecruiting()).isFalse();
        });
    }

    @Test
    void ChallengeFilterConverter는_문자열을_필터로_변환하고_잘못된_값은_거부한다() {
        // given
        ChallengeFilterConverter converter = new ChallengeFilterConverter();

        // then
        assertSoftly(softly -> {
            softly.assertThat(converter.convert(null)).isEqualTo(ChallengeFilter.DEFAULT);
            softly.assertThat(converter.convert("")).isEqualTo(ChallengeFilter.DEFAULT);
            softly.assertThat(converter.convert("summary")).isEqualTo(ChallengeFilter.SUMMARY);
            softly.assertThat(converter.convert("DEFAULT")).isEqualTo(ChallengeFilter.DEFAULT);
            softly.assertThatThrownBy(() -> converter.convert("unknown"))
                    .isInstanceOf(CIllegalArgumentException.class);
        });
    }

    @Test
    void 알림_도메인은_PENDING_초기값과_메타데이터를_보관한다() {
        // when
        ChallengeStartNotification startNotification = ChallengeStartNotification.createPending(
                1L,
                10L,
                "챌린지"
        );
        ChallengeTodoReminderNotification todoReminder = ChallengeTodoReminderNotification.createPending(
                1L,
                10L,
                "챌린지",
                ChallengeTodoReminderPhase.FIRST,
                7,
                2,
                1,
                true
        );

        // then
        assertSoftly(softly -> {
            softly.assertThat(startNotification.getStatus()).isEqualTo(NotificationStatus.PENDING);
            softly.assertThat(startNotification.getAttempts()).isZero();
            softly.assertThat(startNotification.getChallengeName()).isEqualTo("챌린지");
            softly.assertThat(todoReminder.getPhase()).isEqualTo(ChallengeTodoReminderPhase.FIRST);
            softly.assertThat(todoReminder.getStatus()).isEqualTo(NotificationStatus.PENDING);
            softly.assertThat(todoReminder.getStreak()).isEqualTo(7);
            softly.assertThat(todoReminder.getDaysSinceLastParticipation()).isEqualTo(2);
            softly.assertThat(todoReminder.getRemainingAbsences()).isEqualTo(1);
            softly.assertThat(todoReminder.isLastDay()).isTrue();
        });
    }

    private static ChallengeDailyResult result(ChallengeDailyStatus status, LocalDate date) {
        return ChallengeDailyResult.builder()
                .participantId(1L)
                .date(date)
                .status(status)
                .build();
    }

    private static Challenge challenge(Long id, LocalDate startDate, LocalDate endDate, int totalDays) {
        return Challenge.builder()
                .id(id)
                .name("챌린지" + id)
                .generation(1)
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(totalDays)
                .newsletterGroupId(1L)
                .build();
    }
}
