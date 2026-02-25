package me.bombom.api.v1.challenge.service;


import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.dto.TeamTodayProgressCount;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeTeamService {

    private final ChallengeTeamRepository challengeTeamRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final Clock clock;

    @Transactional
    public void updateTeamProgress(ChallengeTeam challengeTeam) {
        TeamTodayProgressCount progressCount = challengeParticipantRepository.findTeamTodayProgressCount(
                challengeTeam.getId(),
                LocalDate.now(clock),
                ChallengeDailyStatus.COMPLETE
        );

        int averageProgress = calculateTodayAverageProgress(progressCount);
        challengeTeam.updateProgress(averageProgress);
    }

    public ChallengeTeam getChallengeTeamByParticipant(ChallengeParticipant participant){
        return challengeTeamRepository.findById(participant.getChallengeTeamId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.CHALLENGE_TEAM_ID, participant.getChallengeTeamId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam")
                        .addContext(ErrorContextKeys.OPERATION, "findById"));
    }

    @Transactional
    public void resetTeamsProgress(List<Challenge> ongoingChallenges) {
        if (ongoingChallenges.isEmpty()) {
            return;
        }

        List<Long> challengeIds = ongoingChallenges.stream()
                .map(Challenge::getId)
                .toList();
        challengeTeamRepository.resetProgressByChallengeIdIn(challengeIds);
    }

    private int calculateTodayAverageProgress(TeamTodayProgressCount progressCount) {
        if (progressCount.survivedCount() == 0) {
            return 0;
        }

        return (int) (progressCount.completedTodayCount() * 100 / progressCount.survivedCount());
    }
}
