package me.bombom.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * 페이징된 아티클 목록 응답
 */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-05-28T15:54:19.996007+09:00[Asia/Seoul]",
    comments = "Generator version: 7.10.0"
)
public record PageArticleResponse(

        @NotNull
        @Valid
        List<@Valid ArticleResponse> content,

        @NotNull
        @Schema(description = "전체 데이터 개수")
        Long totalElements,

        @NotNull
        @Schema(description = "전체 페이지 수")
        Integer totalPages,

        @NotNull
        @Schema(description = "현재 페이지 번호 (0-base)")
        Integer number,

        @NotNull
        @Schema(description = "페이지 크기")
        Integer size,

        @NotNull
        @Schema(description = "현재 페이지 데이터 개수")
        Integer numberOfElements,

        @NotNull
        @Schema(description = "첫 페이지 여부")
        Boolean first,

        @NotNull
        @Schema(description = "마지막 페이지 여부")
        Boolean last
) {
}

