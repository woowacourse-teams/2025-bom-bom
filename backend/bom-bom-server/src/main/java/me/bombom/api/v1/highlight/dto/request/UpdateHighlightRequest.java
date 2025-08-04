package me.bombom.api.v1.highlight.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateHighlightRequest(
    String color,
    @Size(max = 500) String memo
) {
}
