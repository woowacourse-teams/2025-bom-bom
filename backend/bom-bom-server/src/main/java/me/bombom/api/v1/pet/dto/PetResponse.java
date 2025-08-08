package me.bombom.api.v1.pet.dto;

import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;

public record PetResponse(
    int level,
    int currentStageScore,
    int requiredStageScore,
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
