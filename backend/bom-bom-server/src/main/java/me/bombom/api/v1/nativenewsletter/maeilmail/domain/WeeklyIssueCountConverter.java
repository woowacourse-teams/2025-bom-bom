package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class WeeklyIssueCountConverter implements AttributeConverter<WeeklyIssueCount, Integer> {

    @Override
    public Integer convertToDatabaseColumn(WeeklyIssueCount attribute) {
        return attribute.getValue();
    }

    @Override
    public WeeklyIssueCount convertToEntityAttribute(Integer dbData) {
        return WeeklyIssueCount.from(dbData);
    }
}
