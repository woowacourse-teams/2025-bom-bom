package me.bombom.api.v1.highlight.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record HighlightLocation(
        String startOffset,
        String startPath,
        String endOffset,
        String endPath
) {
}
