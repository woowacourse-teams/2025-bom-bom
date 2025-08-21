package me.bombom.api.v1.pet.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
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
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "Pet"));
        Stage currentStage = stageRepository.findById(pet.getStageId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext("stageId", pet.getStageId())
                    .addContext(ErrorContextKeys.OPERATION, "findCurrentStage")
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "Pet"));
        Stage nextStage = stageRepository.findNextStageByCurrentScore(pet.getCurrentScore())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext("currentScore", pet.getCurrentScore())
                    .addContext(ErrorContextKeys.OPERATION, "findNextStage")
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "Pet"));
        return PetResponse.of(pet, currentStage, nextStage);
    }

    @Transactional
    public void attend(Member member) {
        Pet pet = petRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "Pet")
                    .addContext(ErrorContextKeys.OPERATION, "attendance"));
        if(pet.isAttended()){
            throw new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                .addContext("alreadyAttended", true);
        }
        pet.markAsAttended();
        increaseCurrentScore(member.getId(), ScorePolicyConstants.ATTENDANCE_SCORE);
    }

    @Transactional
    public void increaseCurrentScore(Long memberId, int score){
        Pet pet = petRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext("scoreToAdd", score)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "Pet")
                    .addContext(ErrorContextKeys.OPERATION, "increaseScore"));
        pet.increaseCurrentScore(score);
        updatePetStage(pet);
    }

    @Transactional
    public void increaseCurrentScoreForGuideMail(Long memberId, int score){
        Pet pet = petRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                        .addContext("scoreToAdd", score)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "Pet")
                        .addContext(ErrorContextKeys.OPERATION, "increaseScoreForGuideMail"));
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
                    .addContext(ErrorContextKeys.MEMBER_ID, pet.getMemberId())
                    .addContext("currentScore", pet.getCurrentScore())
                    .addContext(ErrorContextKeys.OPERATION, "updatePetStage"));
        pet.updateStage(stageByScore);
    }
}
