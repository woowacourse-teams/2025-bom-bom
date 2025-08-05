package me.bombom.api.v1.highlight.dto.request;

import static me.bombom.api.v1.highlight.controller.HighlightController.COLOR_HEX_PATTERN;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record HighlightCreateRequest(
        @NotNull HighlightLocationRequest location,
        @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId,
        @Pattern(regexp = COLOR_HEX_PATTERN) String color,
        @NotNull String text,
        @Size(max = 500) String memo
) {
}
