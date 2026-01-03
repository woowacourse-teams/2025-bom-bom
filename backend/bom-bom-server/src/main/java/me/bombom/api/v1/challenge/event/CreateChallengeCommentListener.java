package me.bombom.api.v1.challenge.event;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.service.ChallengeParticipantService;
import me.bombom.api.v1.challenge.service.ChallengeTeamService;
import me.bombom.api.v1.challenge.service.ChallengeTodoService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateChallengeCommentListener {

    private final ChallengeTodoService challengeTodoService;
    private final ChallengeParticipantService challengeParticipantService;
    private final ChallengeTeamService challengeTeamService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(CreateChallengeCommentEvent event){
        log.info("챌린지 코멘트 작성 후 출석 처리 시작");

        LocalDate today = LocalDate.now();
        if(challengeTodoService.isCompletedToday(event.participantId(), today)){
            log.info("이미 출석 처리 완료된 참여자입니다. participantId:{}", event.participantId());
            return;
        }

        ChallengeParticipant participant = challengeParticipantService.getParticipant(event.participantId());
        completeDailyTodoWithComment(participant, today);

        ChallengeTeam challengeTeam = challengeTeamService.getChallengeTeamByParticipant(participant);
        challengeTeamService.updateTeamProgress(challengeTeam);

        log.info("챌린지 코멘트 작성 후 출석 처리 완료");
    }

    private void completeDailyTodoWithComment(ChallengeParticipant participant, LocalDate today) {
        try {
            challengeTodoService.insertCommentDone(participant, today);
            challengeTodoService.completeDailyTodo(participant, today);
        } catch (Exception e) {
            log.warn("Comment에 대해 ChallengeDailyTodo 완료 도중 에러가 발생했습니다. participantId: {}", participant.getId());
        }
    }
}
