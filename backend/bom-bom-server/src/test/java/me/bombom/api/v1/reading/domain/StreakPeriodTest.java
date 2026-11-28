package me.bombom.api.v1.reading.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class StreakPeriodTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 11, 27);

    @Test
    void 오늘을_포함한_최근_N일_기간을_만든다() {
        StreakPeriod period = StreakPeriod.recent(TODAY, 7);

        assertThat(period.startDate()).isEqualTo(LocalDate.of(2026, 11, 21));
        assertThat(period.endDate()).isEqualTo(TODAY);
        assertThat(period.dates()).hasSize(7);
    }

    @Test
    void 하루만_조회하면_오늘만_포함한다() {
        StreakPeriod period = StreakPeriod.recent(TODAY, 1);

        assertThat(period.dates()).containsExactly(TODAY);
    }

    @Test
    void 조회_시각_범위는_시작일_0시부터_다음날_0시_전까지다() {
        StreakPeriod period = StreakPeriod.recent(TODAY, 7);

        assertThat(period.startInclusive()).isEqualTo(LocalDateTime.of(2026, 11, 21, 0, 0));
        assertThat(period.endExclusive()).isEqualTo(LocalDateTime.of(2026, 11, 28, 0, 0));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 32})
    void 허용_범위_밖의_일수는_만들_수_없다(int days) {
        assertThatThrownBy(() -> StreakPeriod.recent(TODAY, days))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
