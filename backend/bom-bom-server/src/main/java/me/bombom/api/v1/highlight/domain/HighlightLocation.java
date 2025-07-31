package me.bombom.api.v1.highlight.domain;

import jakarta.persistence.Embeddable;

@Embeddable
public record HighlightLocation(
        String startOffset,
        String startXPath,
        String endOffset,
        String endXPath
) {
}
