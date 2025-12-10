package me.bombom.api.v1.article.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record UpdateWarningSettingRequest(

        @Schema(type = "boolean", description = "아티클 수 임계 경고 알림 설정 여부", required = true)
        boolean isVisible
) {
}
