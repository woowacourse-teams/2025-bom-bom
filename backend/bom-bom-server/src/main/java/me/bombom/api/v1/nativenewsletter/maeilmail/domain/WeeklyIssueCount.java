package me.bombom.api.v1.nativenewsletter.maeilmail.domain;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Getter
@RequiredArgsConstructor
public enum WeeklyIssueCount {

    FIVE(5),
    ONE(1)
    ;

    private final int value;

    public static WeeklyIssueCount from(int value) {
        return Arrays.stream(values())
                .filter(count -> count.value == value)
                .findFirst()
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE)
                        .addContext(ErrorContextKeys.DETAIL, "WeeklyIssueCount 유효성 검사에 실패했습니다."));
    }
}
