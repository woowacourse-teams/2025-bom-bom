package me.bombom.api.v1.article.dto;

import java.time.LocalDate;

public record GetArticlesOptions(
        LocalDate date,
        Long categoryId,
        String keyword
) {

    public static GetArticlesOptions of(LocalDate date, Long categoryId, String keyword) {
        return new GetArticlesOptions(date, categoryId, keyword);
    }
}
