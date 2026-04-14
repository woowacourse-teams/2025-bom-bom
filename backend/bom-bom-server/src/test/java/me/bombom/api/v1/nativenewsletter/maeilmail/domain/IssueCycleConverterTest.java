package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class IssueCycleConverterTest {

    private final IssueCycleConverter converter = new IssueCycleConverter();

    @ParameterizedTest
    @CsvSource({"WEEKDAY, 5", "WEEKLY, 1"})
    void IssueCycle을_DB_컬럼값으로_변환한다(IssueCycle issueCycle, int expected) {
        assertThat(converter.convertToDatabaseColumn(issueCycle)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"5, WEEKDAY", "1, WEEKLY"})
    void DB_컬럼값을_IssueCycle로_변환한다(int dbValue, IssueCycle expected) {
        assertThat(converter.convertToEntityAttribute(dbValue)).isEqualTo(expected);
    }
}
