package me.bombom.api.v1.highlight.dto.request;

import static me.bombom.api.v1.highlight.controller.HighlightController.COLOR_HEX_PATTERN;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record HighlightCreateRequest(
        @NotNull String startOffset,
        @NotNull String startXPath,
        @NotNull String endOffset,
        @NotNull String endXPath,
        @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId,
        @Pattern(regexp = COLOR_HEX_PATTERN) String color,
        @NotNull String text
) {
}
