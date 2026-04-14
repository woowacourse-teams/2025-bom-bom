package me.bombom.api.v1.nativenewsletter.domain;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class IssueCycleConverter implements AttributeConverter<IssueCycle, Integer> {

    @Override
    public Integer convertToDatabaseColumn(IssueCycle attribute) {
        return attribute.getValue();
    }

    @Override
    public IssueCycle convertToEntityAttribute(Integer dbData) {
        return IssueCycle.from(dbData);
    }
}
