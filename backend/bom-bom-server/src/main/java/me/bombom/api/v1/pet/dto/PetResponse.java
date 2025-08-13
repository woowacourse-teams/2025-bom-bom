package me.bombom.api.v1.pet.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;

public record PetResponse(
    @Schema(type = "integer", format = "int32", description = "펫 레벨", required = true)
    int level,
    
    @Schema(type = "integer", format = "int32", description = "현재 스테이지 점수", required = true)
    int currentStageScore,
    
    @Schema(type = "integer", format = "int32", description = "필요한 스테이지 점수", required = true)
    int requiredStageScore,
    
    @Schema(type = "boolean", description = "출석 여부", required = true)
    boolean isAttended
) {
    public static PetResponse of(Pet pet, Stage currentStage, Stage nextStage) {
        int currentStageScore = pet.getCurrentScore() - currentStage.getRequiredScore();
        int requiredStageScore = nextStage.getRequiredScore() - currentStage.getRequiredScore();

        return new PetResponse(
            currentStage.getLevel(),
            currentStageScore,
            requiredStageScore,
            pet.isAttended()
        );
    }
}
