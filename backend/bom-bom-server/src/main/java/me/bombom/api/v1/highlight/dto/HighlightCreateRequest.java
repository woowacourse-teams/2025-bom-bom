package me.bombom.api.v1.highlight.dto;

public record HighlightCreateRequest(
        String startOffset,
        String startPath,
        String endOffset,
        String endPath,
        Long articleId,
        String color,
        String text
) {
}
