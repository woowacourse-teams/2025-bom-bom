package me.bombom.openapi.common;

public final class PetResponseMapper {

    private PetResponseMapper() {
    }

    public static me.bombom.openapi.model.PetResponse toApi(
            me.bombom.api.v1.pet.dto.PetResponse response
    ) {
        return new me.bombom.openapi.model.PetResponse(
                response.level(),
                response.currentStageScore(),
                response.requiredStageScore(),
                response.isAttended()
        );
    }
}
