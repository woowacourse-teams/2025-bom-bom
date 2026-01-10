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
                .build();

        // when
        int passedDays = challenge.calculatePassedDays(targetDate);

        // then
        // 금(1) + 월(1) = 2일
        assertSoftly(softly -> {
            softly.assertThat(passedDays).isEqualTo(2);

            int passedDaysWithWeekend = (int) (ChronoUnit.DAYS.between(challenge.getStartDate(), targetDate) + 1);
            softly.assertThat(passedDays).isNotEqualTo(passedDaysWithWeekend);
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
                .build();

        // when
        int passedDays = challenge.calculatePassedDays(targetDate);

        // then
        assertThat(passedDays).isEqualTo(3);
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
                .build();

        // when
        int passedDays = challenge.calculatePassedDays(targetDate);

        // then
        assertThat(passedDays).isEqualTo(0);
    }
}
