package news.bombom.challenge.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.challenge.domain.ChallengeStartNotification;
import news.bombom.challenge.repository.ChallengeStartNotificationRepository;
import news.bombom.notification.dto.response.NotificationResultResponse;
import news.bombom.notification.service.NotificationStatusHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChallengeStartNotificationStatusService
        implements NotificationStatusHandler<ChallengeStartNotification> {

    private final ChallengeStartNotificationRepository notificationRepository;

    @Override
    @Transactional
    public void updateStatus(ChallengeStartNotification notification, NotificationResultResponse result) {
        if (result.successCount() > 0) {
            notification.markSent();
            notificationRepository.save(notification);
            log.info("챌린지 시작 알림 발송 완료: notificationId={}, 성공={}, 실패={}, 스킵={}",
                    notification.getId(), result.successCount(), result.failCount(), result.skippedCount());
            return;
        }
        if (result.skippedCount() == result.totalDevices()) {
            notification.markSent();
            notificationRepository.save(notification);
            log.info("챌린지 시작 알림 모든 기기 스킵: notificationId={}, 스킵={}",
                    notification.getId(), result.skippedCount());
            return;
        }

        notification.markFailed(result.errorMessages());
        notificationRepository.save(notification);
        log.error("챌린지 시작 알림 모든 기기 발송 실패: notificationId={}", notification.getId());
    }

    @Override
    @Transactional
    public void markAsFailed(ChallengeStartNotification notification, String reason) {
        log.warn("챌린지 시작 알림 FCM 토큰이 없습니다: memberId={}", notification.getMemberId());
        notification.markFailed(reason);
        notificationRepository.save(notification);
    }
}
