package me.bombom.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;

/**
 * NewsletterSummaryResponse
 */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-05-28T15:54:19.996007+09:00[Asia/Seoul]",
    comments = "Generator version: 7.10.0"
)
public record NewsletterSummaryResponse(

        @NotNull
        @Schema(description = "뉴스레터명")
        String name,

        @Schema(description = "이미지 URL")
        String imageUrl,

        @NotNull
        @Schema(description = "카테고리")
        String category
) {
}

