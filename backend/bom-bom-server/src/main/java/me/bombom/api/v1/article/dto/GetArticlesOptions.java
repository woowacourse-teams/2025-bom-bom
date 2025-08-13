package me.bombom.api.v1.article.dto;

import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record GetArticlesOptions(
        @DateTimeFormat(iso = ISO.DATE) LocalDate date,
        String newsletter,
        String keyword
) {

    public static GetArticlesOptions of(LocalDate date, String newsletter, String keyword) {
        return new GetArticlesOptions(date, newsletter, keyword);
    }
}
