package news.bombomemail.article.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.article.service.RecentArticleService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class RecentArticleEventListener {

    private final RecentArticleService recentArticleService;

    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void onArticleArrived(ArticleArrivedEvent event) {
        try {
            recentArticleService.save(event.message(), event.contents(), event.memberId(), event.newsletterId());

            log.info("최신 아티클 저장 완료: 멤버 ID={}, 뉴스레터 ={}, 아티클 제목={}",
                    event.memberId(), event.newsletterName(), event.articleTitle());
        } catch (Exception e) {
            // v2에서 저장 실패 고려 예정
            log.error("아티클 저장 실패: 멤버 ID={}, 뉴스레터={}, 아티클 제목={}",
                    event.memberId(), event.newsletterName(), event.articleTitle(), e);
        }
    }
}
