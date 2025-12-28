package me.bombom.api.v1.challenge.service;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.dto.GetChallengeInfoResponse;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ChallengeService {

    //TODO: 이후에 수료 처리 등 구현 시 관리 방법 고려
    private static final int SUCCESS_REQUIRED_RATIO = 80;

    private final ChallengeRepository challengeRepository;

    public GetChallengeInfoResponse getChallengeInfo(Long id) {
        Challenge challenge = challengeRepository.findById(id)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "getChallengeInfo"));

        return GetChallengeInfoResponse.of(challenge, SUCCESS_REQUIRED_RATIO);
    }
}
