package me.bombom.api.v1.challenge.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeParticipantService {

    private final ChallengeParticipantRepository challengeParticipantRepository;

    public ChallengeParticipant getParticipant(Long participantId){
        return challengeParticipantRepository.findById(participantId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext("challengePariticpantId", participantId));
    }
}
