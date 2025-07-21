package me.bombom.api.v1.article.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SortOption {

    DESC("desc"),
    ASC("asc");

    private final String value;
}
