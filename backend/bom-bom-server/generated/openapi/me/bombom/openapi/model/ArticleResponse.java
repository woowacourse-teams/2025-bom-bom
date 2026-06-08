package me.bombom.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * ArticleResponse
 */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-05-28T15:54:19.996007+09:00[Asia/Seoul]",
    comments = "Generator version: 7.10.0"
)
public record ArticleResponse(

        @NotNull
        @Schema(description = "아티클 ID")
        Long articleId,

        @NotNull
        @Schema(description = "아티클 제목")
        String title,

        @NotNull
        @Schema(description = "본문 요약")
        String contentsSummary,

        @NotNull
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        @Schema(description = "도착 일시")
        LocalDateTime arrivedDateTime,

        @Schema(description = "썸네일 이미지 URL")
        String thumbnailUrl,

        @NotNull
        @Schema(description = "예상 읽기 시간 (분)")
        Integer expectedReadTime,

        @NotNull
        @Schema(description = "읽음 여부")
        Boolean isRead,

        @NotNull
        @Schema(description = "북마크 여부")
        Boolean isBookmarked,

        @NotNull
        @Valid
        NewsletterSummaryResponse newsletter
) {
}

