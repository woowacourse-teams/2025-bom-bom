package me.bombom.openapi.model;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.annotation.Generated;
import jakarta.validation.constraints.NotNull;

/**
 * PetResponse
 */
@Generated(
    value = "org.openapitools.codegen.languages.SpringCodegen",
    date = "2026-05-28T15:47:59.982157+09:00[Asia/Seoul]",
    comments = "Generator version: 7.10.0"
)
public record PetResponse(

        @NotNull
        @Schema(description = "펫 레벨")
        Integer level,

        @NotNull
        @Schema(description = "현재 스테이지 점수")
        Integer currentStageScore,

        @NotNull
        @Schema(description = "필요한 스테이지 점수")
        Integer requiredStageScore,

        @NotNull
        @Schema(description = "출석 여부")
        Boolean isAttended
) {
}

