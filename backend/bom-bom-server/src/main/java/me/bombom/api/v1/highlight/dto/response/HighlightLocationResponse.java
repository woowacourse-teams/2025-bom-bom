package me.bombom.api.v1.highlight.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.highlight.domain.HighlightLocation;

public record HighlightLocationResponse(
        @Schema(type = "integer", format = "int32", description = "시작 오프셋", required = true)
        int startOffset,

        @NotNull
        @Schema(type = "string", description = "시작 XPath", required = true)
        String startXPath,
        
        @Schema(type = "integer", format = "int32", description = "끝 오프셋", required = true)
        int endOffset,

        @NotNull
        @Schema(type = "string", description = "끝 XPath", required = true)
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
