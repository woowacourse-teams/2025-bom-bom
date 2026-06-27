package me.bombom.api.v1.challenge.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.Test;

class ChallengeTest {

    @Test
    void 시작일과_종료일_사이에_주말이_포함되어_있으면_주말을_제외하고_계산한다() {
        // given
        // 금요일 시작
        LocalDate startDate = LocalDate.of(2024, 1, 5);
        // 다음주 월요일까지 계산 (금, 토, 일, 월) -> 4일 경과
        LocalDate targetDate = LocalDate.of(2024, 1, 8);

        Challenge challenge = Challenge.builder()
                .name("Test Challenge")
                .startDate(startDate)
                .endDate(startDate.plusDays(10))
                .totalDays(11)
                .newsletterGroupId(1L)
                .build();

        // when
        int passedWeekDays = challenge.calculatePassedWeekDays(targetDate);

        // then
        // 금(1) + 월(1) = 2일
        assertSoftly(softly -> {
            softly.assertThat(passedWeekDays).isEqualTo(2);

            int passedDaysWithWeekend = (int) (ChronoUnit.DAYS.between(challenge.getStartDate(), targetDate) + 1);
            softly.assertThat(passedWeekDays).isNotEqualTo(passedDaysWithWeekend);
        });
    }

    @Test
    void 시작일과_종료일이_모두_평일이고_주말이_없으면_날짜_차이만큼_계산한다() {
        // given
        // 월요일 시작
        LocalDate startDate = LocalDate.of(2024, 1, 8);
        // 수요일까지 (월, 화, 수) -> 3일
        LocalDate targetDate = LocalDate.of(2024, 1, 10);

        Challenge challenge = Challenge.builder()
                .name("Test Challenge")
                .startDate(startDate)
                .endDate(startDate.plusDays(10))
                .totalDays(11)
                .newsletterGroupId(1L)
                .build();

        // when
        int passedWeekDays = challenge.calculatePassedWeekDays(targetDate);

        // then
        assertThat(passedWeekDays).isEqualTo(3);
    }

    @Test
    void 타겟_날짜가_시작일보다_전이면_0을_반환한다() {
        // given
        LocalDate startDate = LocalDate.of(2024, 1, 8);
        LocalDate targetDate = LocalDate.of(2024, 1, 7);

        Challenge challenge = Challenge.builder()
                .name("Test Challenge")
                .startDate(startDate)
                .endDate(startDate.plusDays(10))
                .totalDays(11)
                .newsletterGroupId(1L)
                .build();

        // when
        int passedWeekDays = challenge.calculatePassedWeekDays(targetDate);

        // then
        assertThat(passedWeekDays).isEqualTo(0);
    }

    @Test
    void 챌린지_상태와_기간_정책을_날짜별로_판별한다() {
        // given
        LocalDate startDate = LocalDate.of(2026, 1, 5);
        LocalDate endDate = LocalDate.of(2026, 1, 16);
        Challenge comingSoon = challenge(null, null);
        Challenge challenge = challenge(startDate, endDate);

        // then
        assertSoftly(softly -> {
            softly.assertThat(comingSoon.getStatus(startDate)).isEqualTo(ChallengeStatus.COMING_SOON);
            softly.assertThat(challenge.getStatus(startDate.minusDays(1))).isEqualTo(ChallengeStatus.BEFORE_START);
            softly.assertThat(challenge.getStatus(startDate)).isEqualTo(ChallengeStatus.ONGOING);
            softly.assertThat(challenge.getStatus(endDate.plusDays(1))).isEqualTo(ChallengeStatus.COMPLETED);
            softly.assertThat(challenge.isWithinPeriod(startDate)).isTrue();
            softly.assertThat(challenge.isWithinPeriod(endDate.plusDays(1))).isFalse();
            softly.assertThat(challenge.hasReachedEnd(endDate)).isTrue();
            softly.assertThat(challenge.hasReachedEnd(startDate)).isFalse();
        });
    }

    @Test
    void 챌린지_모집_단계는_시작전_지각모집_마감으로_판별한다() {
        // given
        LocalDate startDate = LocalDate.of(2026, 1, 5);
        Challenge challenge = challenge(startDate, LocalDate.of(2026, 1, 16));
        Challenge comingSoon = challenge(null, null);

        // then
        assertSoftly(softly -> {
            softly.assertThat(comingSoon.calculatePassedWeekDays(LocalDate.of(2026, 1, 5))).isZero();
            softly.assertThat(comingSoon.isEnded(LocalDate.of(2026, 1, 5))).isFalse();
            softly.assertThat(challenge.getRegistrationPhase(startDate.minusDays(1)))
                    .isEqualTo(RegistrationPhase.EARLY);
            softly.assertThat(challenge.getRegistrationPhase(startDate)).isEqualTo(RegistrationPhase.LATE);
            softly.assertThat(challenge.getRegistrationPhase(startDate.plusDays(2)))
                    .isEqualTo(RegistrationPhase.CLOSED);
            softly.assertThat(challenge.isLatePhase(startDate)).isTrue();
            softly.assertThat(challenge.isRegistrationClosed(startDate.plusDays(2))).isTrue();
            softly.assertThat(challenge.isEnded(LocalDate.of(2026, 1, 17))).isTrue();
        });
    }

    @Test
    void 챌린지_뱃지_발급_상태와_허용_결석수를_갱신한다() {
        // given
        Challenge challenge = challenge(LocalDate.of(2026, 1, 5), LocalDate.of(2026, 1, 16));

        // when
        challenge.markBadgeAsIssued();

        // then
        assertSoftly(softly -> {
            softly.assertThat(challenge.isBadgeIssued()).isTrue();
            softly.assertThat(challenge.calculateMaxAllowedAbsences()).isEqualTo(2);
            softly.assertThat(challenge.isLastDay(LocalDate.of(2026, 1, 16))).isTrue();
            softly.assertThat(challenge.isLastDay(LocalDate.of(2026, 1, 15))).isFalse();
        });
    }

    private static Challenge challenge(LocalDate startDate, LocalDate endDate) {
        return Challenge.builder()
                .name("Test Challenge")
                .startDate(startDate)
                .endDate(endDate)
                .totalDays(10)
                .newsletterGroupId(1L)
                .build();
    }
}
