package news.bombom.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.domain.ArticleArrivalNotification;
import news.bombom.notification.dto.response.NotificationResultResponse;
import news.bombom.notification.repository.ArticleArrivalNotificationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationStatusService {

    private final ArticleArrivalNotificationRepository notificationRepository;

    @Transactional
    public void updateStatus(ArticleArrivalNotification notification, NotificationResultResponse result) {
        if (result.successCount() > 0) {
            notification.markSent();
            notificationRepository.save(notification);
            log.info("알림 발송 완료: notificationId={}, 성공={}, 실패={}, 스킵={}",
                    notification.getId(), result.successCount(), result.failCount(), result.skippedCount());
            return;
        }
        if (result.skippedCount() == result.totalDevices()) {
            // 모든 기기가 알림 설정이 꺼져있으면 SENT로 처리 (정상적인 상황)
            notification.markSent();
            notificationRepository.save(notification);
            log.info("모든 기기에서 알림 설정이 꺼져있음: notificationId={}, 스킵={}",
                    notification.getId(), result.skippedCount());
            return;
        }
        // 모든 기기에서 실패하면 FAILED로 처리
        notification.markFailed(result.errorMessages());
        notificationRepository.save(notification);
        log.error("모든 기기에서 알림 발송 실패: notificationId={}", notification.getId());
    }

    @Transactional
    public void markAsFailed(ArticleArrivalNotification notification, String reason) {
        log.warn("FCM 토큰이 없습니다: memberId={}", notification.getMemberId());
        notification.markFailed(reason);
        notificationRepository.save(notification);
    }
}
