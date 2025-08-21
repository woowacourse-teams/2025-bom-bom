package me.bombom.api.v1.newsletter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record NewsletterResponse(

        @NotNull
        @Schema(type = "integer", format = "int64", description = "뉴스레터 ID", required = true)
        Long newsletterId,

        @NotNull
        @Schema(type = "string", description = "뉴스레터명", required = true)
        String name,

        @Schema(type = "string", description = "이미지 URL")
        String imageUrl,

        @NotNull
        @Schema(type = "string", description = "설명", required = true)
        String description,

        @NotNull
        @Schema(type = "string", description = "구독 URL", required = true)
        String subscribeUrl,

        @NotNull
        @Schema(type = "string", description = "카테고리", required = true)
        String category
) {
}
