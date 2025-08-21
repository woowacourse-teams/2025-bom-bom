package me.bombom.api.v1.newsletter.dto;

import com.querydsl.core.annotations.QueryProjection;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record NewsletterSummaryResponse(

        @NotNull
        @Schema(type = "string", description = "뉴스레터명", required = true)
        String name,

        @Schema(type = "string", description = "이미지 URL")
        String imageUrl,

        @NotNull
        @Schema(type = "string", description = "카테고리", required = true)
        String category
) {

    @QueryProjection
    public NewsletterSummaryResponse {}
}
