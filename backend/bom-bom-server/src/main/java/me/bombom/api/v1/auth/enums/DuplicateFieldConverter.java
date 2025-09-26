package me.bombom.api.v1.auth.enums;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class DuplicateFieldConverter implements Converter<String, SignupValidateField> {

    @Override
    public SignupValidateField convert(String source) {
        if (source == null) {
            return null;
        }
        return SignupValidateField.valueOf(source.strip().toUpperCase());
    }
}
