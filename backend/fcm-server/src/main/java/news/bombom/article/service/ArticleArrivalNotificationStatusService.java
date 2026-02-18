package news.bombom.article.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.article.domain.ArticleArrivalNotification;
import news.bombom.article.domain.ArticleArrivalNotificationFailed;
import news.bombom.article.repository.ArticleArrivalNotificationFailedRepository;
import news.bombom.article.repository.ArticleArrivalNotificationRepository;
import news.bombom.notification.dto.response.NotificationResultResponse;
import news.bombom.notification.service.NotificationStatusHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArticleArrivalNotificationStatusService implements NotificationStatusHandler<ArticleArrivalNotification> {

    private final ArticleArrivalNotificationRepository notificationRepository;
    private final ArticleArrivalNotificationFailedRepository articleArrivalNotificationFailedRepository;

    @Override
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
            notification.markSent();
            notificationRepository.save(notification);
            log.info("모든 기기에서 알림 설정이 꺼져있음: notificationId={}, 스킵={}",
                    notification.getId(), result.skippedCount());
            return;
        }

        notification.markFailed(result.errorMessages());
        notificationRepository.save(notification);
        log.error("모든 기기에서 알림 발송 실패: notificationId={}", notification.getId());
        moveToFailedTableIfExceeded(notification);
    }

    @Override
    @Transactional
    public void markAsFailed(ArticleArrivalNotification notification, String reason) {
        log.warn("FCM 토큰이 없습니다: memberId={}", notification.getMemberId());
        notification.markFailed(reason);
        notificationRepository.save(notification);
        moveToFailedTableIfExceeded(notification);
    }

    @Transactional
    public void moveToFailedTableIfExceeded(ArticleArrivalNotification notification) {
        if (!notification.shouldRetry()) {
            moveToFailedTable(notification);
        }
    }

    private void moveToFailedTable(ArticleArrivalNotification notification) {
        ArticleArrivalNotificationFailed failed = ArticleArrivalNotificationFailed.from(notification);
        articleArrivalNotificationFailedRepository.save(failed);
        notificationRepository.delete(notification);
        log.warn("최대 재시도 횟수 초과로 인한 데이터 격리: notificationId={}", notification.getId());
    }
}
