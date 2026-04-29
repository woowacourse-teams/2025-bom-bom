package me.bombom.api.v1.common.util;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class DateUtilsTest {

    @Test
    void 토요일과_일요일이면_주말이다() {
        // given
        LocalDate saturday = LocalDate.of(2026, 4, 25);
        LocalDate sunday = LocalDate.of(2026, 4, 26);

        // when & then
        assertSoftly(softly -> {
            softly.assertThat(DateUtils.isWeekend(saturday)).isTrue();
            softly.assertThat(DateUtils.isWeekend(sunday)).isTrue();
        });
    }

    @Test
    void 월요일부터_금요일이면_주말이_아니다() {
        // when & then
        assertSoftly(softly -> {
            softly.assertThat(DateUtils.isWeekend(LocalDate.of(2026, 4, 20))).isFalse();
            softly.assertThat(DateUtils.isWeekend(LocalDate.of(2026, 4, 21))).isFalse();
            softly.assertThat(DateUtils.isWeekend(LocalDate.of(2026, 4, 22))).isFalse();
            softly.assertThat(DateUtils.isWeekend(LocalDate.of(2026, 4, 23))).isFalse();
            softly.assertThat(DateUtils.isWeekend(LocalDate.of(2026, 4, 24))).isFalse();
        });
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        // when & then
        assertThatThrownBy(() -> DateUtils.isWeekend(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("date는 null일 수 없습니다.");
    }
}
