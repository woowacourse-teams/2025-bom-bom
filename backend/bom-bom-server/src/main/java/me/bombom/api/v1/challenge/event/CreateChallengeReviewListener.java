package me.bombom.api.v1.challenge.event;

import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.challenge.domain.ChallengeParticipant;
import me.bombom.api.v1.challenge.domain.ChallengeTeam;
import me.bombom.api.v1.challenge.service.ChallengeParticipantService;
import me.bombom.api.v1.challenge.service.ChallengeTeamService;
import me.bombom.api.v1.challenge.service.ChallengeTodoService;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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

    private static final String UK_DAILY_TODO = "uk_challenge_daily_todo";

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

        ChallengeParticipant participant = challengeParticipantService.getParticipant(event.participantId());
        boolean alreadyCompleted = challengeTodoService.isCompletedToday(event.participantId(), event.reviewDate());
        insertReviewDone(participant, event.reviewDate());

        if (alreadyCompleted) {
            log.info("이미 출석 처리 완료된 참여자입니다. participantId:{}", event.participantId());
            return;
        }

        challengeTodoService.completeDailyTodo(event.participantId(), event.reviewDate());

        ChallengeTeam challengeTeam = challengeTeamService.getByParticipant(participant);
        challengeTeamService.updateTeamProgress(challengeTeam);

        log.info("챌린지 리뷰 작성 후 출석 처리 완료 participantId={}", event.participantId());
    }

    private void insertReviewDone(ChallengeParticipant participant, LocalDate reviewDate) {
        try {
            challengeTodoService.insertReviewDone(participant, reviewDate);
        } catch (DataIntegrityViolationException e) {
            String violated = extractConstraintName(e);

            if (UK_DAILY_TODO.equalsIgnoreCase(violated)) {
                log.warn("challenge TODO가 이미 존재합니다. -> skip. participantId={}, constraint={}", participant.getId(), violated);
                return;
            }
            throw e;
        }
    }

    private String extractConstraintName(Throwable e) {
        Throwable cur = e;
        while (cur != null) {
            if (cur instanceof ConstraintViolationException cve) {
                return cve.getConstraintName();
            }
            cur = cur.getCause();
        }
        return null;
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
