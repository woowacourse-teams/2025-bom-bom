package me.bombom.api.v1.signupsource.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Schema.RequiredMode;

public record SignupSourceStatusResponse(

        @Schema(description = "가입 경로 질문을 띄워야 하는지", requiredMode = RequiredMode.REQUIRED)
        boolean needsSignupSource
) {
}
