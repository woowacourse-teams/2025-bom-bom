package news.bombomemail.subscribe.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.subscribe.service.NewsletterSubscriptionCountService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class NewsletterSubscriptionCountEventListener {

    private final NewsletterSubscriptionCountService newsletterSubscriptionCountService;

    @TransactionalEventListener
    public void on(NewsletterSubscriptionCountEvent event) {
        try {
            newsletterSubscriptionCountService.updateNewsletterSubscriptionCount(event.newsletterId(), event.memberId());
        } catch (Exception e) {
            // FIXME :: 로깅 시스템 구축후 추가될 예정
            log.error("뉴스레터 구독 카운팅 저장 실패");
        }
    }
}
