package me.bombom.api.v1.highlight.dto.response;

import java.util.List;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.highlight.domain.HighlightLocation;

public record HighlightResponse(
        Long id,
        HighlightLocation location,
        Long articleId,
        String color,
        String text,
        String memo
) {

    public static List<HighlightResponse> from(List<Highlight> highlights) {
        return highlights.stream()
                .map(HighlightResponse::from)
                .toList();
    }

    public static HighlightResponse from(Highlight highlight) {
        return new HighlightResponse(
                highlight.getId(),
                highlight.getHighlightLocation(),
                highlight.getArticleId(),
                highlight.getColor().getValue(),
                highlight.getText(),
                highlight.getMemo()
        );
    }
}
