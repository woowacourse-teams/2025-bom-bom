package me.bombom.api.v1.newsletter.domain;

import java.util.Arrays;
import java.util.Optional;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

/** 번역 테이블에 저장하는 언어는 ISO 639-1 두 글자 코드로 관리한다. 한국어는 원본 컬럼을 사용한다. */
public enum NewsletterLanguage {
    EN("en");

    private final String code;

    NewsletterLanguage(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static NewsletterLanguage from(String code) {
        return Arrays.stream(values())
                .filter(language -> language.code.equals(code))
                .findFirst()
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION)
                        .addContext("language", code)
                        .addContext("supportedLanguages", "ko, en"));
    }

    public static Optional<NewsletterLanguage> fromRequest(String code) {
        if ("ko".equals(code)) {
            return Optional.empty();
        }
        return Optional.of(from(code));
    }
}
