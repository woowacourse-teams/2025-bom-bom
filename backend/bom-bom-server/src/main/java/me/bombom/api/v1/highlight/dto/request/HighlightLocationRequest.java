package me.bombom.api.v1.highlight.dto.request;


import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.highlight.domain.HighlightLocation;

public record HighlightLocationRequest(
        @NotNull String startOffset,
        @NotNull String startXPath,
        @NotNull String endOffset,
        @NotNull String endXPath
) {

    public HighlightLocation toHighlightLocation() {
        return new HighlightLocation(startOffset, startXPath, endOffset, endXPath);
    }
}
