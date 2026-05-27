package me.bombom.api.v1.challenge.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.service.ChallengeParticipantService;
import me.bombom.api.v1.challenge.service.ChallengeTeamService;
import me.bombom.api.v1.challenge.service.ChallengeTodoService;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateChallengeReviewListener {

    private final ChallengeTodoService challengeTodoService;
    private final ChallengeParticipantService challengeParticipantService;
    private final ChallengeTeamService challengeTeamService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 500, multiplier = 2.0)
    )
    public void on(CreateChallengeReviewEvent event) {
        log.info("챌린지 리뷰 작성 후 출석 처리 시작 participantId={}, reviewDate={}",
                event.participantId(), event.reviewDate());

        if (challengeTodoService.isCompletedToday(event.participantId(), event.reviewDate())) {
            log.info("이미 출석 처리 완료된 참여자입니다. participantId:{}", event.participantId());
            return;
        }

        ChallengeParticipant participant = challengeParticipantService.getParticipant(event.participantId());
        challengeTodoService.insertReviewDone(participant, event.reviewDate());
        challengeTodoService.completeDailyTodo(event.participantId(), event.reviewDate());

        ChallengeTeam challengeTeam = challengeTeamService.getByParticipant(participant);
        challengeTeamService.updateTeamProgress(challengeTeam);

        log.info("챌린지 리뷰 작성 후 출석 처리 완료 participantId={}", event.participantId());
    }

    @Recover
    public void recover(Exception e, CreateChallengeReviewEvent event) {
        log.error(
                "챌린지 리뷰 출석 처리 최종 실패. 수동 복구 필요. participantId={}, reviewDate={}, errMsg={}",
                event.participantId(),
                event.reviewDate(),
                e.getMessage(),
                e
        );
    }
}
