package me.bombom.api.v1.article.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record PreviousArticleRequest(

        @NotNull
        @Positive(message = "newsletterId는 1 이상의 값이어야 합니다.")
        Long newsletterId,

        @Max(value = 10)
        @Positive(message = "limit는 1 이상의 값이어야 합니다.")
        int limit
) {
}
