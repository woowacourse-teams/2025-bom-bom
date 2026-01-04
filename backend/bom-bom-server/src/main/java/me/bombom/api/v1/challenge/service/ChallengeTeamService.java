package me.bombom.api.v1.challenge.service;


import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
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

    private final ChallengeRepository challengeRepository;
    private final ChallengeTeamRepository challengeTeamRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;

    @Transactional
    public void updateTeamProgress(ChallengeTeam challengeTeam) {
        Challenge challenge = challengeRepository.findById(challengeTeam.getChallengeId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, challengeTeam.getChallengeId()));

        List<ChallengeParticipant> teamParticipants = challengeParticipantRepository.findAllByChallengeTeamId(challengeTeam.getId());
        if (teamParticipants.isEmpty()) {
            throw new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                    .addContext(ErrorContextKeys.CHALLENGE_TEAM_ID, challengeTeam.getId())
                    .addContext(ErrorContextKeys.OPERATION, "findAllByChallengeTeamId");
        }

        int averageProgress = calculateAverageProgress(teamParticipants, challenge.getTotalDays());
        challengeTeam.updateProgress(averageProgress);
    }

    public ChallengeTeam getChallengeTeamByParticipant(ChallengeParticipant participant){
        return challengeTeamRepository.findById(participant.getChallengeTeamId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.CHALLENGE_TEAM_ID, participant.getChallengeTeamId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam")
                        .addContext(ErrorContextKeys.OPERATION, "findById"));
    }

    private int calculateAverageProgress(List<ChallengeParticipant> participants, int totalDays) {
        int totalProgress = participants.stream()
                .mapToInt(teamMember -> teamMember.calculateProgress(totalDays))
                .sum();
        return totalProgress / participants.size();
    }
}
