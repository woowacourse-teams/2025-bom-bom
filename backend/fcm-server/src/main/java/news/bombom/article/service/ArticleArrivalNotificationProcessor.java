package news.bombom.article.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.article.domain.ArticleArrivalNotification;
import news.bombom.article.repository.ArticleArrivalNotificationRepository;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.scheduler.NotificationProcessor;
import news.bombom.notification.service.NotificationProcessingService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ArticleArrivalNotificationProcessor implements NotificationProcessor {

    private final ArticleArrivalNotificationRepository notificationRepository;
    private final NotificationProcessingService notificationProcessingService;
    private final ArticleArrivalNotificationStatusService articleStatusService;

    @Override
    public String type() {
        return NotificationCategory.ARTICLE.name();
    }

    @Override
    public void processPendingNotifications(LocalDateTime now) {
        List<ArticleArrivalNotification> pendingNotifications =
                notificationRepository.findRetryCandidates(
                        List.of(NotificationStatus.PENDING, NotificationStatus.FAILED),
                        now
                );

        log.info("[{}] 처리할 알림 개수: {}", type(), pendingNotifications.size());

        for (ArticleArrivalNotification notification : pendingNotifications) {
            try {
                if (!notification.shouldRetry()) {
                    log.warn("[{}] 최대 재시도 횟수 초과: notificationId={}, attempts={}",
                            type(), notification.getId(), notification.getAttempts());
                    articleStatusService.moveToFailedTableIfExceeded(notification);
                    continue;
                }

                notificationProcessingService.processNotification(
                        notification,
                        NotificationCategory.ARTICLE,
                        articleStatusService
                );
            } catch (Exception e) {
                log.error("[{}] 알림 처리 중 오류 발생: notificationId={}", type(), notification.getId(), e);
            }
        }
    }
}
