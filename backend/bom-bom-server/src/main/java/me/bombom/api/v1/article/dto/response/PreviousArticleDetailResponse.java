package me.bombom.api.v1.article.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import me.bombom.api.v1.newsletter.dto.NewsletterBasicResponse;

public record PreviousArticleDetailResponse(

        @NotNull
        String title,

        @NotNull
        String contents,

        @NotNull
        LocalDateTime arrivedDateTime,
        
        @Schema(required = true)
        int expectedReadTime,

        @NotNull
        NewsletterBasicResponse newsletter
) {
}
