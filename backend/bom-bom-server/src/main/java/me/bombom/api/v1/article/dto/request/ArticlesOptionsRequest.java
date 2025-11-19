package me.bombom.api.v1.article.dto.request;

import jakarta.validation.constraints.Positive;

public record ArticlesOptionsRequest(

        @Positive(message = "id는 1 이상의 값이어야 합니다.")
        Long newsletterId
) {

    public static ArticlesOptionsRequest of(Long newsletterId) {
        return new ArticlesOptionsRequest(newsletterId);
    }
}
