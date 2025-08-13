package me.bombom.api.v1.highlight.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import me.bombom.api.v1.highlight.domain.Highlight;

public record HighlightResponse(
        @Schema(type = "integer", format = "int64", description = "하이라이트 ID", required = true)
        Long id,
        
        @Schema(type = "object", description = "하이라이트 위치 정보", required = true)
        HighlightLocationResponse location,
        
        @Schema(type = "integer", format = "int64", description = "아티클 ID", required = true)
        Long articleId,
        
        @Schema(type = "string", description = "하이라이트 색상", required = true)
        String color,
        
        @Schema(type = "string", description = "하이라이트된 텍스트", required = true)
        String text,
        
        @Schema(type = "string", description = "메모", required = true)
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
