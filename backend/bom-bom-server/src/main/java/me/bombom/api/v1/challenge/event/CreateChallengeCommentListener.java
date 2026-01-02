package me.bombom.api.v1.challenge.event;

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
import me.bombom.api.v1.challenge.repository.ChallengeParticipantRepository;
import me.bombom.api.v1.challenge.repository.ChallengeTodoRepository;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorContextKeys;
import me.bombom.api.v1.common.exception.ErrorDetail;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateChallengeCommentListener {

    private final ChallengeTodoRepository challengeTodoRepository;
    private final ChallengeDailyTodoRepository challengeDailyTodoRepository;
    private final ChallengeDailyResultRepository challengeDailyResultRepository;
    private final ChallengeParticipantRepository challengeParticipantRepository;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(CreateChallengeCommentEvent event){
        LocalDate today = LocalDate.now();
        log.info("챌린지 코멘트 작성 후 출석 처리 시작");

        // 0. participant 조회하기
        ChallengeParticipant participant = challengeParticipantRepository.findById(event.participantId())
                .orElseThrow(() -> new CServerErrorException(ErrorDetail.ENTITY_NOT_FOUND)
                        .addContext(ErrorContextKeys.OPERATION, "findById"));

        // 이미 코멘트를 작성했으면 중복으로 출석 처리 안하도록 return
        if(challengeDailyResultRepository.existsByParticipantIdAndDate(participant.getId(), today)){
            log.info("이미 출석 처리 완료된 참여자입니다. participantId:{}", participant.getId());
            return;
        }

        // 1. ChallengeDailyTodo에 todoType을 COMMENT로 insert
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

        // 2. ChallengeDailyResult에 해당 participant로 데이터 추가
        challengeDailyResultRepository.save(
                ChallengeDailyResult.builder()
                .participantId(participant.getId())
                .date(today)
                .status(ChallengeDailyStatus.COMPLETE)
                .build()
        );

        // 3. ChallengeParticipant에서 누적 성공 일수 업데이트
        participant.increaseCompletedDays();

        log.info("챌린지 코멘트 작성 후 출석 처리 완료");
    }
}
