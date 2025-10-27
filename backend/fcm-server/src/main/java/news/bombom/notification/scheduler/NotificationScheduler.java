package news.bombom.notification.scheduler;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.repository.ArticleArrivalNotificationRepository;
import news.bombom.notification.service.NotificationProcessingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationScheduler {

    private final ArticleArrivalNotificationRepository notificationRepository;
    private final NotificationProcessingService notificationProcessingService;

    @Scheduled(fixedDelay = 30000)
    public void processPendingNotifications() {
        List<ArticleArrivalNotification> pendingNotifications =
                notificationRepository.findRetryCandidates(
                        List.of(NotificationStatus.PENDING, NotificationStatus.FAILED),
                        LocalDateTime.now()
                );

        log.info("처리할 알림 개수: {}", pendingNotifications.size());

        for (ArticleArrivalNotification notification : pendingNotifications) {
            try {
                if (!notification.shouldRetry()) {
                    log.warn("최대 재시도 횟수 초과: notificationId={}, attempts={}",
                            notification.getId(), notification.getAttempts());
                    continue;
                }

                notificationProcessingService.processNotification(notification);
            } catch (Exception e) {
                log.error("알림 처리 중 오류 발생: notificationId={}", notification.getId(), e);
            }
        }
    }
}
