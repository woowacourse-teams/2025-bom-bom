package me.bombom.api.v1.auth.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DuplicateFieldConverter implements Converter<String, DuplicateCheckField> {

    @Override
    public DuplicateCheckField convert(String source) {
        if (source == null) {
            return null;
        }
        return DuplicateCheckField.valueOf(source.strip().toUpperCase());
    }
}
