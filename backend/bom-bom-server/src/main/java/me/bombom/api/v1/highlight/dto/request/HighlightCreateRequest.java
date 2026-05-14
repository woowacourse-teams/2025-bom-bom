package me.bombom.api.v1.highlight.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.highlight.domain.Color;

public record HighlightCreateRequest(

        @NotNull
        HighlightLocationRequest location,

        @Positive(message = "id는 1 이상의 값이어야 합니다.")
        Long articleId,

        @NotNull 
        @Schema(description = "하이라이트 색상 (HEX 형식, 예: #FF0000)", type = "string", example = "#FFD6C2")
        Color color,

        @NotNull
        String text,

        @Size(max = 500)
        String memo
) {
}
