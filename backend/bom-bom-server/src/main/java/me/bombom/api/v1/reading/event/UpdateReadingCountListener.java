package me.bombom.api.v1.reading.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.ArticleService;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UpdateReadingCountListener {

    private final ArticleService articleService;
    private final ReadingService readingService;

    @TransactionalEventListener
    public void on(UpdateReadingCountEvent event) {
        log.info("읽기 횟수 이벤트 received - memberId={}, articleId={}", event.getMemberId(), event.getArticleId());
        try {
            boolean isTodayArticle = articleService.isArrivedToday(event.getArticleId(), event.getMemberId());
            readingService.updateReadingCount(event.getMemberId(), isTodayArticle);
            log.info("읽기 횟수 갱신 성공 - memberId={}, articleId={}, isTodayArticle={}",
                    event.getMemberId(), event.getArticleId(), isTodayArticle);
        } catch (Exception e) {
            log.error("읽기 횟수 갱신 실패 - memberId={}, articleId={}", event.getMemberId(), event.getArticleId(), e);
        }
    }
}
