package me.bombom.api.v1.challenge.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.event.MarkAsReadEvent;
import me.bombom.api.v1.challenge.service.ChallengeDailyTodoService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeParticipantTodoListener {

    private final ChallengeDailyTodoService challengeDailyTodoService;

    @TransactionalEventListener
    public void on(MarkAsReadEvent event) {
        try {
            challengeDailyTodoService.updateChallengeDailyTodo(event.memberId());
        } catch (Exception e) {
            log.error("챌린지 데일리 투두 저장 중 오류가 발생했습니다. memberId={}", event.memberId(), e);
        }
    }
}
