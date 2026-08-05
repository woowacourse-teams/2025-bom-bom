package me.bombom.api.v1.article.dto.request;

import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.format.annotation.DateTimeFormat.ISO;

public record ArticlesOptionsRequest(

        @DateTimeFormat(iso = ISO.DATE)
        LocalDate date,

        @Positive(message = "id는 1 이상의 값이어야 합니다.")
        Long newsletterId,

        Boolean unreadOnly
) {

    public ArticlesOptionsRequest {
        unreadOnly = Boolean.TRUE.equals(unreadOnly);
    }

    public static ArticlesOptionsRequest of(LocalDate date, Long newsletterId) {
        return new ArticlesOptionsRequest(date, newsletterId, false);
    }

    public static ArticlesOptionsRequest of(LocalDate date, Long newsletterId, boolean unreadOnly) {
        return new ArticlesOptionsRequest(date, newsletterId, unreadOnly);
    }
}
