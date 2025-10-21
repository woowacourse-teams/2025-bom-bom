package news.bombomemail.email.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.notification.domain.ArticleArrivalNotification;
import news.bombomemail.notification.domain.NotificationStatus;
import news.bombomemail.notification.repository.ArticleArrivalNotificationRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleArrivedEventListener {

    private final ArticleArrivalNotificationRepository articleArrivalNotificationRepository;

    @TransactionalEventListener
    public void onArticleArrived(ArticleArrivedEvent event) {
        try {
            ArticleArrivalNotification articleArrivalNotification = ArticleArrivalNotification.builder()
                    .memberId(event.memberId())
                    .newsletterName(event.newsletterName())
                    .articleTitle(event.articleTitle())
                    .status(NotificationStatus.PENDING)
                    .attempts(0)
                    .isRead(false)
                    .build();
            articleArrivalNotificationRepository.save(articleArrivalNotification);
            
            log.info("아티클 도착 알림 저장 완료: 멤버 ID={}, 뉴스레터={}, 아티클 제목={}",
                    event.memberId(), event.newsletterName(), event.articleTitle());
        } catch (Exception e) {
            // v2에서 저장 실패 고려 예정
            log.error("아티클 도착 알림 저장 실패: 멤버 ID={}, 뉴스레터={}, 아티클 제목={}",
                    event.memberId(), event.newsletterName(), event.articleTitle(), e);
        }
    }
}
