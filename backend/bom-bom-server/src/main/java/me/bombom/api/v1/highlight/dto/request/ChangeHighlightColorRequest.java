package me.bombom.api.v1.highlight.dto.request;

import static me.bombom.api.v1.highlight.controller.HighlightController.COLOR_HEX_PATTERN;

import jakarta.validation.constraints.Pattern;

public record ChangeHighlightColorRequest(
    @Pattern(regexp = COLOR_HEX_PATTERN)
    String color
) {
} 