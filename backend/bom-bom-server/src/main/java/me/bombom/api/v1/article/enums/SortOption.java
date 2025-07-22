package me.bombom.api.v1.article.enums;

import lombok.Getter;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorDetail;

@Getter
public enum SortOption {

    DESC,
    ASC
    ;

    public static SortOption from(String input) {
        for (SortOption option : values()) {
            if (option.name().equalsIgnoreCase(input)) {
                return option;
            }
        }
        throw new CIllegalArgumentException(ErrorDetail.INVALID_INPUT_VALUE);
    }
}
