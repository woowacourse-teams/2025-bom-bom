package me.bombom.api.v1.highlight.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import me.bombom.api.v1.highlight.domain.Color;

public record UpdateHighlightRequest(

        @Schema(description = "하이라이트 색상 (HEX 형식, 예: #FF0000)", type = "string", example = "#FFD6C2")
        Color color,

        @Size(max = 500)
        String memo
) {
}
