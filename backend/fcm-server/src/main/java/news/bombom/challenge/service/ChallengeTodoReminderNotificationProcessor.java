package news.bombom.challenge.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.challenge.domain.ChallengeTodoReminderNotification;
import news.bombom.challenge.repository.ChallengeTodoReminderNotificationRepository;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.scheduler.NotificationProcessor;
import news.bombom.notification.service.NotificationProcessingService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeTodoReminderNotificationProcessor implements NotificationProcessor {

    private final ChallengeTodoReminderNotificationRepository notificationRepository;
    private final NotificationProcessingService notificationProcessingService;
    private final ChallengeTodoReminderNotificationStatusService challengeStatusService;

    @Override
    public String type() {
        return NotificationCategory.CHALLENGE_TODO_REMINDER.name();
    }

    @Override
    public void processPendingNotifications(LocalDateTime now) {
        List<ChallengeTodoReminderNotification> pendingNotifications =
                notificationRepository.findRetryCandidates(
                        List.of(NotificationStatus.PENDING, NotificationStatus.FAILED),
                        now
                );

        log.info("[{}] 처리할 알림 개수: {}", type(), pendingNotifications.size());

        for (ChallengeTodoReminderNotification notification : pendingNotifications) {
            try {
                if (!notification.shouldRetry()) {
                    log.warn("[{}] 최대 재시도 횟수 초과로 처리 중단: notificationId={}, attempts={}",
                            type(), notification.getId(), notification.getAttempts());
                    continue;
                }

                notificationProcessingService.processNotification(
                        notification,
                        NotificationCategory.CHALLENGE_TODO_REMINDER,
                        challengeStatusService
                );
            } catch (Exception e) {
                log.error("[{}] 알림 처리 중 오류 발생: notificationId={}", type(), notification.getId(), e);
            }
        }
    }
}
