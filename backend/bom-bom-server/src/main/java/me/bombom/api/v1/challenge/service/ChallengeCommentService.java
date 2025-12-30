package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.ChallengeCommentResponse;
import me.bombom.api.v1.challenge.repository.ChallengeCommentRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeCommentService {

    private final ChallengeCommentRepository challengeCommentRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;

    public Page<ChallengeCommentResponse> getChallengeComments(
            Long challengeId,
            Long memberId,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    ){
        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, memberId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.FORBIDDEN_RESOURCE)
                    .addContext(ErrorContextKeys.MEMBER_ID, memberId)
                    .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndMemberId"));
        return challengeCommentRepository.findAllByTeamInDuration(participant.getChallengeTeamId(), memberId, startDate, endDate, pageable);
    }
}
