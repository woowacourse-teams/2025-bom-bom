package me.bombom.api.v1.maeilmail.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IssueCycleTest {

    @ParameterizedTest
    @CsvSource({"5, WEEKDAY", "1, WEEKLY"})
    void 숫자로_IssueCycle을_조회한다(int value, IssueCycle expected) {
        assertThat(IssueCycle.from(value)).isEqualTo(expected);
    }

    @Test
    void 지원하지_않는_숫자면_예외가_발생한다() {
        assertThatThrownBy(() -> IssueCycle.from(3))
                .isInstanceOf(CIllegalArgumentException.class);
    }

    @ParameterizedTest
    @CsvSource({"WEEKDAY, 5", "WEEKLY, 1"})
    void IssueCycle의_value가_올바르다(IssueCycle issueCycle, int expectedValue) {
        assertThat(issueCycle.getValue()).isEqualTo(expectedValue);
    }
}
