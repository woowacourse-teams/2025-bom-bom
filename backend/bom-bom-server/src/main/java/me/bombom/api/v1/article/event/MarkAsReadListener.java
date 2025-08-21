package me.bombom.api.v1.article.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.pet.service.PetService;
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

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void on(MarkAsReadEvent event) {
        log.info("MarkAsReadEvent received - memberId={}, articleId={}", event.memberId(), event.articleId());
        try {
            updateReadingCount(event);
            updatePetScore(event);
        } catch (Exception e) {
            log.error("MarkAsReadEvent 처리 실패 - memberId={}, articleId={}", event.memberId(), event.articleId(), e);
        }
    }

    private void updateReadingCount(MarkAsReadEvent event) {
        boolean isTodayArticle = articleService.isArrivedToday(event.articleId(), event.memberId());
        readingService.updateReadingCount(event.memberId(), isTodayArticle);
        log.info("읽기 횟수 갱신 성공 - memberId={}, articleId={}, isTodayArticle={}",
                event.memberId(), event.articleId(), isTodayArticle);
    }

    private void updatePetScore(MarkAsReadEvent event) {
        if(articleService.canAddArticleScore(event.memberId())) {
            int score = readingService.calculateArticleScore(event.memberId());
            petService.increaseCurrentScore(event.memberId(), score);
            log.info("아티클 점수 추가 성공 - memberId={}", event.memberId());
        }
    }
}
