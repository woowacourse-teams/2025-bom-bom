package me.bombom.api.v1.article.dto;

import java.time.LocalDate;
import me.bombom.api.v1.article.enums.SortOption;
import org.springframework.web.bind.annotation.RequestParam;

public record GetArticlesOptions(
        @RequestParam(required = false) LocalDate date,
        @RequestParam(required = false) Long categoryId,
        @RequestParam(required = false, name = "sorted", defaultValue = "desc") SortOption sorted
) {

    public static GetArticlesOptions of(LocalDate date, Long categoryId, SortOption sorted) {
        return new GetArticlesOptions(date, categoryId, sorted);
    }
}
