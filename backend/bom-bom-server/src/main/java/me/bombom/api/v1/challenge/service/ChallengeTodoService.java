package me.bombom.api.v1.challenge.service;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.ChallengeDailyResult;
import me.bombom.api.v1.challenge.domain.ChallengeDailyStatus;
import me.bombom.api.v1.challenge.domain.ChallengeDailyTodo;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTodo;
import me.bombom.api.v1.challenge.domain.ChallengeTodoType;
import me.bombom.api.v1.challenge.repository.ChallengeDailyResultRepository;
import me.bombom.api.v1.challenge.repository.ChallengeDailyTodoRepository;
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
public class ChallengeTodoService {

    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;
    private final ChallengeDailyResultRepository challengeDailyResultRepository;

    public boolean isCompletedToday(Long participantId, LocalDate today) {
        return challengeDailyResultRepository.existsByParticipantIdAndDate(participantId, today);
    }

    @Transactional
    public void insertCommentDone(ChallengeParticipant participant, LocalDate today) {
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

    @Transactional
    public void completeDailyTodo(ChallengeParticipant participant, LocalDate today){
        challengeDailyResultRepository.save(
                ChallengeDailyResult.builder()
                        .participantId(participant.getId())
                        .date(today)
                        .status(ChallengeDailyStatus.COMPLETE)
                        .build()
        );

        participant.increaseCompletedDays();
    }
}
