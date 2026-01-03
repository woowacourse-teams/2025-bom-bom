package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.dto.ChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.TeamChallengeProgressFlat;
import me.bombom.api.v1.challenge.dto.response.MemberChallengeProgressResponse;
import me.bombom.api.v1.challenge.dto.response.TeamChallengeProgressResponse;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
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

    // TODO: 이후에 수료 처리 등 구현 시 관리 방법 고려
    private static final double SUCCESS_REQUIRED_RATIO = 0.8;

    private final ChallengeRepository challengeRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeDailyResultRepository challengeDailyResultRepository;

    @Transactional
    public void proceedDailySurvivalCheck(Challenge challenge, LocalDate yesterday) {
        List<ChallengeParticipant> absentees = challengeParticipantRepository.findAbsentees(challenge.getId(), yesterday);
        for (ChallengeParticipant absentee : absentees) {
            if (absentee.useShieldIfAvailable()) {
                saveShieldDailyResult(absentee, yesterday);
                continue;
            }
            checkFailure(absentee, challenge, yesterday);
        }
    }

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

    public TeamChallengeProgressResponse getTeamProgress(Long challengeId, Member member) {
        Challenge challenge = challengeRepository.findById(challengeId)
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "getTeamProgress"));

        ChallengeParticipant participant = challengeParticipantRepository.findByChallengeIdAndMemberId(challengeId, member.getId())
                .orElseThrow(() -> new CIllegalArgumentException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeParticipant")
                        .addContext(ErrorContextKeys.OPERATION, "getTeamProgress"));

        List<TeamChallengeProgressFlat> progressList = challengeParticipantRepository.findTeamProgress(participant.getChallengeTeamId());

        return TeamChallengeProgressResponse.of(challenge, progressList);
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

    private void saveShieldDailyResult(ChallengeParticipant participant, LocalDate date) {
        ChallengeDailyResult result = ChallengeDailyResult.builder()
                .participantId(participant.getId())
                .date(date)
                .status(ChallengeDailyStatus.SHIELD)
                .build();
        challengeDailyResultRepository.save(result);
    }

    private void checkFailure(ChallengeParticipant participant, Challenge challenge, LocalDate yesterday) {
        // 종료 일은 포함하지 않아서 +1
        int totalDays = challenge.getTotalDays();
        int requiredSuccessDays = (int) Math.ceil(totalDays * SUCCESS_REQUIRED_RATIO);
        int maxAllowedAbsent = totalDays - requiredSuccessDays;

        int daysSinceStart = (int) (ChronoUnit.DAYS.between(challenge.getStartDate(), yesterday) + 1);
        int currentAbsent = daysSinceStart - participant.getCompletedDays();
        if (currentAbsent > maxAllowedAbsent) {
            participant.markAsFailed();
        }
    }
}
