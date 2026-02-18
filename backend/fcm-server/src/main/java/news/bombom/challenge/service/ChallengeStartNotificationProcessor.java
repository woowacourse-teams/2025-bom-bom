package news.bombom.challenge.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.challenge.domain.ChallengeStartNotification;
import news.bombom.challenge.repository.ChallengeStartNotificationRepository;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationStatus;
import news.bombom.notification.scheduler.NotificationProcessor;
import news.bombom.notification.service.NotificationProcessingService;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeStartNotificationProcessor implements NotificationProcessor {

    private final ChallengeStartNotificationRepository notificationRepository;
    private final NotificationProcessingService notificationProcessingService;
    private final ChallengeStartNotificationStatusService challengeStatusService;

    @Override
    public String type() {
        return NotificationCategory.CHALLENGE_START.name();
    }

    @Override
    public void processPendingNotifications(LocalDateTime now) {
        List<ChallengeStartNotification> pendingNotifications =
                notificationRepository.findRetryCandidates(
                        List.of(NotificationStatus.PENDING, NotificationStatus.FAILED),
                        now
                );

        log.info("[{}] 처리할 알림 개수: {}", type(), pendingNotifications.size());

        for (ChallengeStartNotification notification : pendingNotifications) {
            try {
                if (!notification.shouldRetry()) {
                    log.warn("[{}] 최대 재시도 횟수 초과로 처리 중단: notificationId={}, attempts={}",
                            type(), notification.getId(), notification.getAttempts());
                    continue;
                }

                notificationProcessingService.processNotification(
                        notification,
                        NotificationCategory.CHALLENGE_START,
                        challengeStatusService
                );
            } catch (Exception e) {
                log.error("[{}] 알림 처리 중 오류 발생: notificationId={}", type(), notification.getId(), e);
            }
        }
    }
}
