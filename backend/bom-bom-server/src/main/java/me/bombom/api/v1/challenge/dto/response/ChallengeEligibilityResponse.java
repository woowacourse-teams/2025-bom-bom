package me.bombom.api.v1.challenge.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import me.bombom.api.v1.challenge.domain.EligibilityReason;

public record ChallengeEligibilityResponse(

        @Schema(type = "boolean", description = "신청 가능 여부", required = true)
        boolean canApply,

        @NotNull
        EligibilityReason reason
) {
}
