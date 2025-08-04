package me.bombom.api.v1.highlight.dto.request;

import jakarta.validation.constraints.Size;
import me.bombom.api.v1.highlight.domain.Color;

public record UpdateHighlightRequest(
    Color color,
    @Size(max = 500) String memo
) {
}
