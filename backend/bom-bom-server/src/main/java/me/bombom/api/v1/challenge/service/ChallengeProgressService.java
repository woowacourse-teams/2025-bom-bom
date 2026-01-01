package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.member.domain.Member;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeProgressService {

    private final ChallengeParticipantRepository challengeParticipantRepository;

    public MemberChallengeProgressResponse getMemberProgress(Long id, Member member) {
        List<ChallengeProgressFlat> progressList = challengeParticipantRepository.findMemberProgress(
                id,
                member.getId(),
                LocalDate.now()
        );

        if (progressList.isEmpty()) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                    .addContext(ErrorContextKeys.OPERATION, "getMemberProgress")
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId())
                    .addContext(ErrorContextKeys.CHALLENGE_ID, id);
        }

        return MemberChallengeProgressResponse.of(member, progressList);
    }
}
