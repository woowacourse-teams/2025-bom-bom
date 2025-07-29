package me.bombom.api.v1.article.dto;

import java.time.LocalDate;

public record GetArticlesOptions(
        LocalDate date,
        String category,
        String keyword
) {

    public static GetArticlesOptions of(LocalDate date, String category, String keyword) {
        return new GetArticlesOptions(date, category, keyword);
    }
}
