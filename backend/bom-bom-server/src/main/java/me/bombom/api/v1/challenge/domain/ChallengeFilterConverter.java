package me.bombom.api.v1.challenge.domain;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class ChallengeFilterConverter implements Converter<String, ChallengeFilter> {

    @Override
    public ChallengeFilter convert(String source) {
        if (source == null || source.isBlank()) {
            return ChallengeFilter.DEFAULT;
        }
        if ("summary".equalsIgnoreCase(source)) {
            return ChallengeFilter.SUMMARY;
        }
        return ChallengeFilter.DEFAULT;
    }
}
