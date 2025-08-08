package me.bombom.api.v1.highlight.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import me.bombom.api.v1.highlight.domain.Highlight;

public record HighlightResponse(
        Long id,
        HighlightLocationResponse location,
        Long articleId,
        String color,
        String text,
        String memo
) {

    @QueryProjection
    public HighlightResponse {
    }

    public static List<HighlightResponse> from(List<Highlight> highlights) {
        return highlights.stream()
                .map(HighlightResponse::from)
                .toList();
    }

    public static HighlightResponse from(Highlight highlight) {
        return new HighlightResponse(
                highlight.getId(),
                HighlightLocationResponse.from(highlight.getHighlightLocation()),
                highlight.getArticleId(),
                highlight.getColor().getValue(),
                highlight.getText(),
                highlight.getMemo()
        );
    }
}
