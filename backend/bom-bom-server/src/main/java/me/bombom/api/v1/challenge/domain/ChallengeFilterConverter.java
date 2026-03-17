package me.bombom.api.v1.challenge.domain;

import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class ChallengeFilterConverter implements Converter<String, ChallengeFilter> {

    @Override
    public ChallengeFilter convert(String source) {
        if (!StringUtils.hasText(source)) {
            return ChallengeFilter.DEFAULT;
        }
        if ("summary".equalsIgnoreCase(source)) {
            return ChallengeFilter.SUMMARY;
        }
        if ("default".equalsIgnoreCase(source)) {
            return ChallengeFilter.DEFAULT;
        }
        throw new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION);
    }
}
