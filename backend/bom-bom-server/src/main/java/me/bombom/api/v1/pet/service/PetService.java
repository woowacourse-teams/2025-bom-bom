package me.bombom.api.v1.pet.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.pet.ScorePolicyConstants;
import me.bombom.api.v1.pet.domain.Pet;
import me.bombom.api.v1.pet.domain.Stage;
import me.bombom.api.v1.pet.dto.PetResponse;
import me.bombom.api.v1.pet.repository.PetRepository;
import me.bombom.api.v1.pet.repository.StageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final StageRepository stageRepository;

    public PetResponse getPet(Member member) {
        Pet pet = petRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", member.getId())
                    .addContext("entityType", "Pet"));
        Stage currentStage = stageRepository.findById(pet.getStageId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("memberId", member.getId())
                    .addContext("stageId", pet.getStageId())
                    .addContext("operation", "findCurrentStage"));
        Stage nextStage = stageRepository.findNextStageByCurrentScore(pet.getCurrentScore())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("memberId", member.getId())
                    .addContext("currentScore", pet.getCurrentScore())
                    .addContext("operation", "findNextStage"));
        return PetResponse.of(pet, currentStage, nextStage);
    }

    @Transactional
    public void attend(Member member) {
        Pet pet = petRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", member.getId())
                    .addContext("entityType", "Pet")
                    .addContext("operation", "attendance"));
        if(pet.isAttended()){
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext("memberId", member.getId())
                .addContext("alreadyAttended", true);
        }
        pet.markAsAttended();
        increaseCurrentScore(member.getId(), ScorePolicyConstants.ATTENDANCE_SCORE);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseCurrentScore(Long memberId, int score){
        Pet pet = petRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext("memberId", memberId)
                    .addContext("scoreToAdd", score)
                    .addContext("entityType", "Pet")
                    .addContext("operation", "increaseScore"));
        pet.increaseCurrentScore(score);
        updatePetStage(pet);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createPet(Long memberId) {
        petRepository.save(Pet.builder()
                .memberId(memberId)
                .stageId(1L) // TODO: stage ID 따른 상수화 필요
                .build());
    }

    @Transactional
    public void resetAttendance() {
        petRepository.resetAllAttendance();
    }

    private void updatePetStage(Pet pet) {
        Stage stageByScore = stageRepository.findCurrentStageByCurrentScore(pet.getCurrentScore())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("memberId", pet.getMemberId())
                    .addContext("currentScore", pet.getCurrentScore())
                    .addContext("operation", "updatePetStage"));
        pet.updateStage(stageByScore);
    }
}
