package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class WeeklyIssueCountConverterTest {

    private final WeeklyIssueCountConverter converter = new WeeklyIssueCountConverter();

    @ParameterizedTest
    @CsvSource({"FIVE, 5", "ONE, 1"})
    void WeeklyIssueCount를_DB_컬럼값으로_변환한다(WeeklyIssueCount weeklyIssueCount, int expected) {
        assertThat(converter.convertToDatabaseColumn(weeklyIssueCount)).isEqualTo(expected);
    }

    @ParameterizedTest
    @CsvSource({"5, FIVE", "1, ONE"})
    void DB_컬럼값을_WeeklyIssueCount로_변환한다(int dbValue, WeeklyIssueCount expected) {
        assertThat(converter.convertToEntityAttribute(dbValue)).isEqualTo(expected);
    }
}
