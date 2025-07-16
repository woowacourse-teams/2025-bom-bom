package me.bombom.api.v1.article.dto;

import java.time.LocalDate;
import me.bombom.api.v1.article.enums.SortOption;

public record GetArticlesOptions(
        LocalDate date,
        Long categoryId,
        SortOption sort
) {
    public static GetArticlesOptions of(LocalDate date, Long categoryId, SortOption sort) {
        return new GetArticlesOptions(date, categoryId, sort);
    }
}
