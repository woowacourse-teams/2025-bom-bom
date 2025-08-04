package me.bombom.api.v1.highlight.dto.request;

import static me.bombom.api.v1.highlight.controller.HighlightController.COLOR_HEX_PATTERN;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UpdateHighlightRequest(
    @Pattern(regexp = COLOR_HEX_PATTERN) String color,
    @Size(max = 500) String memo
) {
}