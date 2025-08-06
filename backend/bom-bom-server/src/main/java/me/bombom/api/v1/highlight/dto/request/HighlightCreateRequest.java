package me.bombom.api.v1.highlight.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.highlight.domain.Color;

public record HighlightCreateRequest(
        @NotNull HighlightLocationRequest location,
        @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId,
        @NotNull Color color,
        @NotNull String text,
        @Size(max = 500) String memo
) {
}
