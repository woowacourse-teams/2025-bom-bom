package me.bombom.api.v1.article.event;

import io.opentelemetry.instrumentation.annotations.WithSpan;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.pet.service.PetService;
import me.bombom.api.v1.reading.service.ReadRateLimitService;
import me.bombom.api.v1.reading.service.ReadingService;
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

    @WithSpan
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(MarkAsReadEvent event) {
        log.info("MarkAsReadEvent received - memberId={}, articleId={}", event.memberId(), event.articleId());

        if (!readRateLimitService.checkAndConsume(event.memberId())) {
            log.info("읽기 rate limit 초과로 카운트 갱신 skip - memberId={}", event.memberId());
            return;
        }

        boolean isTodayArticle = articleService.isArrivedToday(event.articleId(), event.memberId());
        updateReadingCount(event, isTodayArticle);
        updatePetScore(event, isTodayArticle);
    }

    private void updateReadingCount(MarkAsReadEvent event, boolean isTodayArticle) {
        readingService.updateReadingCount(event.memberId(), isTodayArticle);
        log.info("읽기 횟수 갱신 성공 - memberId={}, articleId={}, isTodayArticle={}",
                event.memberId(), event.articleId(), isTodayArticle);
    }

    private void updatePetScore(MarkAsReadEvent event, boolean isTodayArticle) {
        if (!isTodayArticle || !articleService.canAddArticleScore(event.memberId())) {
            return;
        }
        try {
            int score = readingService.calculateArticleScore(event.memberId());
            petService.increaseCurrentScore(event.memberId(), score);
            log.info("아티클 점수 추가 성공 - memberId={}", event.memberId());
        } catch (Exception e) {
            // 펫 경험치는 부가 기능이므로 실패해도 읽기 카운트/토큰 차감은 유지
            log.error("아티클 점수 추가 실패 - memberId={}", event.memberId(), e);
        }
    }
}
