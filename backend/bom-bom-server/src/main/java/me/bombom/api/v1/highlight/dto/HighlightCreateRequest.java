package me.bombom.api.v1.highlight.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;

public record HighlightCreateRequest( //
        String startOffset,
        String startXPath,
        String endOffset,
        String endXPath,
        @Positive(message = "id는 1 이상의 값이어야 합니다.") Long articleId,
        @Pattern(regexp = "^#[0-9a-fA-F]{6}$") String color,
        @NotNull String text
) {
}
