package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.Challenge;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTeamRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeAttendanceService {

    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;
    private final ChallengeDailyResultRepository challengeDailyResultRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;
    private final ChallengeRepository challengeRepository;
    private final ChallengeTeamRepository challengeTeamRepository;

    @Transactional
    public void attend(Long participantId){
        LocalDate today = LocalDate.now();
        ChallengeParticipant participant = challengeParticipantRepository.findById(participantId)
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.OPERATION, "findById"));

        // 이미 코멘트를 작성했으면 중복으로 출석 처리 안하도록 return
        if(challengeDailyResultRepository.existsByParticipantIdAndDate(participant.getId(), today)){
            log.info("이미 출석 처리 완료된 참여자입니다. participantId:{}", participant.getId());
            return;
        }

        insertCommentDone(participant, today);
        insertCompleteDailyResult(participant, today);
        increaseCompletedDays(participant);
        updateTeamProgress(participant);
    }

    private void insertCommentDone(ChallengeParticipant participant, LocalDate today) {
        ChallengeTodo challengeTodo = challengeTodoRepository.findByChallengeIdAndTodoType(participant.getChallengeId(), ChallengeTodoType.COMMENT)
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, participant.getChallengeId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "ChallengeTodo")
                        .addContext(ErrorContextKeys.OPERATION, "findByChallengeIdAndTodoType"));

        ChallengeDailyTodo dailyTodo = ChallengeDailyTodo.builder()
                .participantId(participant.getId())
                .todoDate(today)
                .challengeTodoId(challengeTodo.getId())
                .build();

        challengeDailyTodoRepository.save(dailyTodo);
    }

    private void insertCompleteDailyResult(ChallengeParticipant participant, LocalDate today) {
        challengeDailyResultRepository.save(
                ChallengeDailyResult.builder()
                        .participantId(participant.getId())
                        .date(today)
                        .status(ChallengeDailyStatus.COMPLETE)
                        .build()
        );
    }

    private void increaseCompletedDays(ChallengeParticipant participant) {
        participant.increaseCompletedDays();
    }

    private void updateTeamProgress(ChallengeParticipant participant) {
        Challenge challenge = challengeRepository.findById(participant.getChallengeId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.CHALLENGE_ID, participant.getChallengeId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challenge")
                        .addContext(ErrorContextKeys.OPERATION, "findById"));

        ChallengeTeam challengeTeam = challengeTeamRepository.findById(participant.getChallengeTeamId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext("challengeTeamId", participant.getChallengeTeamId())
                        .addContext(ErrorContextKeys.ENTITY_TYPE, "challengeTeam")
                        .addContext(ErrorContextKeys.OPERATION, "findById"));

        List<ChallengeParticipant> teamParticipants = challengeParticipantRepository.findAllByChallengeTeamId(participant.getChallengeTeamId());
        int averageProgress = calculateAverageProgress(teamParticipants, challenge.getTotalDays());
        challengeTeam.updateProgress(averageProgress);
    }

    private int calculateAverageProgress(List<ChallengeParticipant> participants, int totalDays) {
        if (participants.isEmpty()) {
            return 0;
        }
        int totalProgress = participants.stream()
                .mapToInt(teamMember -> teamMember.calculateProgress(totalDays))
                .sum();
        return totalProgress / participants.size();
    }
}
