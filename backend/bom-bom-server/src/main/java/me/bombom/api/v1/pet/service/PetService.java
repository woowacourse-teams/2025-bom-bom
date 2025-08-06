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
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        Stage stage = stageRepository.findById(pet.getStageId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR));
        return PetResponse.of(pet,stage);
    }

    @Transactional
    public void attend(Member member) {
        Pet pet = petRepository.findByMemberId(member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        pet.updateAttendance(true);
        increaseCurrentScore(member.getId(), ScorePolicyConstants.ATTENDANCE_SCORE);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void increaseCurrentScore(Long memberId, int score){
        Pet pet = petRepository.findByMemberId(memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND));
        pet.increaseCurrentScore(score);
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
        petRepository.findAllByIsAttended(true)
                .forEach(pet -> pet.updateAttendance(false));
    }
}
