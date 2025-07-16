package me.bombom.api.v1.article.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortOption {

    DESC("DESC"),
    ASC("ASC")
    ;

    private final String value;
}
