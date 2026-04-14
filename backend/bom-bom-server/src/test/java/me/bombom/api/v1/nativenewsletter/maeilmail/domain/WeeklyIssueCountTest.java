package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeeklyIssueCountTest {

    @ParameterizedTest
    @CsvSource({"5, FIVE", "1, ONE"})
    void 숫자로_WeeklyIssueCount를_조회한다(int value, WeeklyIssueCount expected) {
        assertThat(WeeklyIssueCount.from(value)).isEqualTo(expected);
    }

    @Test
    void 지원하지_않는_숫자면_예외가_발생한다() {
        assertThatThrownBy(() -> WeeklyIssueCount.from(3))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"FIVE, 5", "ONE, 1"})
    void WeeklyIssueCount의_value가_올바르다(WeeklyIssueCount weeklyIssueCount, int expectedValue) {
        assertThat(weeklyIssueCount.getValue()).isEqualTo(expectedValue);
    }
}
