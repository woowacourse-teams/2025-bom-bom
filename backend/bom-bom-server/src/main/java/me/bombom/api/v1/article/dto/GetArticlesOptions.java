package me.bombom.api.v1.article.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record GetArticlesOptions(
        @DateTimeFormat(iso = ISO.DATE)
        LocalDate date,

        @Positive(message = "id는 1 이상의 값이어야 합니다.")
        Long newsletterId,

        @Size(min = 2, max = 100, message = "검색어는 2자 이상 100자 이하로 입력해야 합니다.")
        @Pattern(regexp = ".*\\S.*", message = "검색어는 빈값 또는 공백만으로 입력할 수 없습니다.")
        String keyword
) {

    public static GetArticlesOptions of(LocalDate date, Long newsletterId, String keyword) {
        return new GetArticlesOptions(date, newsletterId, keyword);
    }
}
