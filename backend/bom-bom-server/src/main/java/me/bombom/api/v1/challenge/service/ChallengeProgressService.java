package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.common.exception.CIllegalArgumentException;
import me.bombom.api.v1.common.exception.CServerErrorException;
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
        validateParticipation(id, member);

        List<ChallengeProgressFlat> progressList = challengeParticipantRepository.findMemberProgress(
                id,
                member.getId(),
                LocalDate.now()
        );
        validateMemberProgressDataIntegrity(id, member, progressList);

        return MemberChallengeProgressResponse.of(member, progressList);
    }

    private void validateParticipation(Long id, Member member) {
        boolean isParticipant = challengeParticipantRepository.existsByChallengeIdAndMemberId(id, member.getId());
        if (!isParticipant) {
            throw new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                    .addContext(ErrorContextKeys.OPERATION, "getMemberProgress")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, id)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId());
        }
    }

    private void validateMemberProgressDataIntegrity(Long id, Member member, List<ChallengeProgressFlat> progressList) {
        if (progressList.isEmpty()) {
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                    .addContext(ErrorContextKeys.OPERATION, "getMemberProgress")
                    .addContext(ErrorContextKeys.DETAIL, "참가자 정보는 존재하나 일일 진행 상황 데이터가 조회되지 않습니다")
                    .addContext(ErrorContextKeys.CHALLENGE_ID, id)
                    .addContext(ErrorContextKeys.MEMBER_ID, member.getId());
        }
    }
}
