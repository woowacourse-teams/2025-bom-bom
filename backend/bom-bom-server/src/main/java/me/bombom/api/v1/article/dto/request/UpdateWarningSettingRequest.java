package me.bombom.api.v1.article.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;

public record UpdateWarningSettingRequest(

        boolean isVisible
) {
}
