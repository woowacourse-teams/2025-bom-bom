package me.bombom.api.v1.highlight.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;
import me.bombom.api.v1.article.domain.Article;
import me.bombom.api.v1.highlight.domain.Highlight;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record HighlightResponse(
        @NotNull
        @Schema(type = "integer", format = "int64", description = "하이라이트 ID", required = true)
        Long id,

        @NotNull
        @Schema(type = "object", description = "하이라이트 위치 정보", required = true)
        HighlightLocationResponse location,

        @NotNull
        @Schema(type = "integer", format = "int64", description = "아티클 ID", required = true)
        Long articleId,

        @NotNull
        @Schema(type = "string", description = "하이라이트 색상", required = true)
        String color,

        @NotNull
        @Schema(type = "string", description = "하이라이트된 텍스트", required = true)
        String text,
        
        @Schema(type = "string", description = "메모")
        String memo,

        @Schema(type = "string", description = "뉴스레터 이름")
        String newsletterName,

        @Schema(type = "string", description = "뉴스레터 이미지 url")
        String newsletterImageUrl,

        @Schema(type = "string", description = "아티클 제목")
        String ariticleTitle,

        @Schema(type = "string", format = "date-time", description = "하이라이트 생성 일시")
        LocalDateTime createdAt
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
                highlight.getMemo(),
                null,
                null,
                null,
                null
        );
    }

    public static HighlightResponse of(Highlight highlight, Article article, Newsletter newsletter){
        return new HighlightResponse(
                highlight.getId(),
                HighlightLocationResponse.from(highlight.getHighlightLocation()),
                highlight.getArticleId(),
                highlight.getColor().getValue(),
                highlight.getText(),
                highlight.getMemo(),
                newsletter.getName(),
                newsletter.getImageUrl(),
                article.getTitle(),
                highlight.getCreatedAt()
        );
    }
}
