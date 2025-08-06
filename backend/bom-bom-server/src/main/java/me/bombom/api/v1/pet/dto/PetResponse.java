package me.bombom.api.v1.pet.dto;

import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;

public record PetResponse(
        int level,
        int totalScore,
        int currentScore,
        boolean isAttended
) {

    public static PetResponse of(Pet pet, Stage stage) {
        return new PetResponse(
                stage.getLevel(),
                stage.getRequiredScore(),
                pet.getCurrentScore(),
                pet.isAttended()
        );
    }
}
