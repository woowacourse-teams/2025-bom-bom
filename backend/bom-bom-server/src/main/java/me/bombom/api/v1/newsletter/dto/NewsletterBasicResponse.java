package me.bombom.api.v1.newsletter.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.newsletter.domain.Category;
import me.bombom.api.v1.newsletter.domain.Newsletter;

public record NewsletterBasicResponse(

        @NotNull
        @Schema(type = "string", description = "뉴스레터명", required = true)
        String name,

        @NotNull
        @Schema(type = "string", description = "이메일", required = true)
        String email,

        @Schema(type = "string", description = "이미지 URL")
        String imageUrl,

        @NotNull
        @Schema(type = "string", description = "카테고리", required = true)
        String category
) {

    public static NewsletterBasicResponse of(Newsletter newsletter, Category category) {
        return new NewsletterBasicResponse(
                newsletter.getName(),
                newsletter.getEmail(),
                newsletter.getImageUrl(),
                category.getName()
        );
    }
}
