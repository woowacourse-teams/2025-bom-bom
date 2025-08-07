package me.bombom.api.v1.highlight.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import me.bombom.api.v1.highlight.domain.HighlightLocation;

public record HighlightLocationResponse(
        int startOffset,
        String startXPath,
        int endOffset,
        String endXPath
) {

    @QueryProjection
    public HighlightLocationResponse {
    }

    public static HighlightLocationResponse from(HighlightLocation highlightLocation) {
        return new HighlightLocationResponse(
                highlightLocation.getStartOffset(),
                highlightLocation.getStartXPath(),
                highlightLocation.getEndOffset(),
                highlightLocation.getEndXPath()
        );
    }
}
