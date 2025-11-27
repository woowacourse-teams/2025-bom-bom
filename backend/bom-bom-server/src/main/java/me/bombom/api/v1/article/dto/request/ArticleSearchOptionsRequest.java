package me.bombom.api.v1.article.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ArticleSearchOptionsRequest(

        @Positive(message = "id는 1 이상의 값이어야 합니다.")
        Long newsletterId,

        @NotBlank(message = "검색 키워드는 필수입니다.")
        @Size(min = 2, max = 100, message = "검색어는 2자 이상 100자 이하로 입력해야 합니다.")
        String keyword
) {

    public static ArticleSearchOptionsRequest of(Long newsletterId, String keyword) {
        return new ArticleSearchOptionsRequest(newsletterId, keyword);
    }
}
