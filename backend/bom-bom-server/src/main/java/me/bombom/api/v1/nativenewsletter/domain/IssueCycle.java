package me.bombom.api.v1.nativenewsletter.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Getter
@RequiredArgsConstructor
public enum IssueCycle {

    WEEKDAY(5),
    WEEKLY(1)
    ;

    private final int value;

    public static IssueCycle from(int value) {
        return Arrays.stream(values())
                .filter(cycle -> cycle.value == value)
                .findFirst()
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                        .addContext(ErrorContextKeys.DETAIL, "IssueCycle 유효성 검사에 실패했습니다."));
    }
}
