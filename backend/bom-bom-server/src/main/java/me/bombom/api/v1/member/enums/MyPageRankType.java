package me.bombom.api.v1.member.enums;

import java.util.Arrays;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

public enum MyPageRankType {
    STREAK("streak"),
    READING("reading");

    private final String value;

    MyPageRankType(String value) {
        this.value = value;
    }

    public String value() {
        return value;
    }

    public static MyPageRankType from(String value) {
        return Arrays.stream(values())
                .filter(type -> type.value.equals(value))
                .findFirst()
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.INVALID_REQUEST_PARAMETER_VALIDATION)
                        .addContext("type", value)
                        .addContext("allowedTypes", "streak, reading"));
    }
}
