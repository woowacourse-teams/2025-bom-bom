package me.bombom.api.v1.article.event;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.article.repository.MarkAsReadEventLogRepository;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadRateLimitService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkAsReadListener {

    private final ArticleService articleService;
    private final ReadingService readingService;
    private final PetService petService;
    private final ReadRateLimitService readRateLimitService;
    private final MarkAsReadEventLogRepository markAsReadEventLogRepository;
    private final DiscordWebhookNotifier discordWebhookNotifier;

    @WithSpan
    @TransactionalEventListener
    @Async("markAsReadExecutor")
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Retryable(
            retryFor = TransientDataAccessException.class,
            maxAttempts = 3, // 최초 호출 포함 3번 시도
            backoff = @Backoff(delay = 500, multiplier = 2, random = true) // 500ms, 1000ms 대기 후 실행 (jitter 포함)
    )
    public void on(MarkAsReadEvent event) {
        log.info("MarkAsReadEvent received - memberId={}, articleId={}", event.memberId(), event.articleId());

        if (!markAsReadEventLogRepository.markIfAbsent(event.memberId(), event.articleId())) {
            log.info("이미 처리된 이벤트 - skip - memberId={}, articleId={}", event.memberId(), event.articleId());
            return;
        }

        boolean isReadCountTokenConsumed = readRateLimitService.tryConsumeReadCountToken(event.memberId(), event.readAt());
        if (isReadCountTokenConsumed) {
            boolean isTodayArticle = articleService.isArrivedToday(event.articleId(), event.memberId(), event.readAt().toLocalDate());
            updateReadingCount(event, isTodayArticle);
            updatePetScore(event, isTodayArticle);
            return;
        }

        log.info("읽기 rate limit 초과로 카운트 갱신 skip - memberId={}", event.memberId());
    }

    @Recover
    void recover(TransientDataAccessException e, MarkAsReadEvent event) {
        log.error("MarkAsRead 재시도 모두 실패 - memberId={}, articleId={}, readAt={}",
                event.memberId(),
                event.articleId(),
                event.readAt(),
                e
        );
        discordWebhookNotifier.sendMarkAsReadErrorNotification(event, e);
    }

    private void updateReadingCount(MarkAsReadEvent event, boolean isTodayArticle) {
        readingService.updateReadingCount(event.memberId(), isTodayArticle);
        log.info("읽기 횟수 갱신 성공 - memberId={}, articleId={}, isTodayArticle={}",
                event.memberId(), event.articleId(), isTodayArticle);
    }

    private void updatePetScore(MarkAsReadEvent event, boolean isTodayArticle) {
        if (!isTodayArticle) {
            return;
        }

        try {
            boolean canRewardArticleRead = articleService.canAddArticleScore(event.memberId());
            if (!canRewardArticleRead) {
                return;
            }

            int score = readingService.calculateArticleScore(event.memberId());
            petService.increaseCurrentScore(event.memberId(), score);
            log.info("아티클 점수 추가 성공 - memberId={}", event.memberId());
        } catch (Exception e) {
            // 펫 경험치는 부가 기능이므로 실패해도 읽기 카운트/토큰 차감은 유지
            log.error("아티클 점수 추가 실패 - memberId={}", event.memberId(), e);
        }
    }
}
