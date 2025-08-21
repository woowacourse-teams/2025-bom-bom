package me.bombom.api.v1.article.dto.request;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ArticleNewsletterStatisticOptionsRequest(

        @Size(min = 2, max = 100, message = "검색어는 2자 이상 100자 이하로 입력해야 합니다.")
        @Pattern(regexp = ".*\\S.*", message = "검색어는 빈값 또는 공백만으로 입력할 수 없습니다.")
        String keyword
) {
}
