package news.bombom.inquiry.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.inquiry.domain.InquiryMessageArrivalNotification;
import news.bombom.notification.dto.response.NotificationResultResponse;
import news.bombom.notification.service.NotificationStatusHandler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryMessageArrivalNotificationStatusService
        implements NotificationStatusHandler<InquiryMessageArrivalNotification> {

    @Override
    @Transactional
    public void updateStatus(InquiryMessageArrivalNotification notification, NotificationResultResponse result) {
        if (result.successCount() > 0) {
            notification.markSent();
            log.info("문의 답변 알림 발송 완료: notificationId={}, 성공={}, 실패={}, 스킵={}",
                    notification.getId(), result.successCount(), result.failCount(), result.skippedCount());
            return;
        }
        if (result.skippedCount() == result.totalDevices()) {
            notification.markSent();
            log.info("문의 답변 알림 모든 기기 스킵: notificationId={}, 스킵={}",
                    notification.getId(), result.skippedCount());
            return;
        }

        notification.markFailed(result.errorMessages());
        log.error("문의 답변 알림 모든 기기 발송 실패: notificationId={}, attempts={}, shouldRetry={}, nextRetryAt={}",
                notification.getId(), notification.getAttempts(), notification.shouldRetry(),
                notification.getNextRetryAt());
    }

    @Override
    @Transactional
    public void markAsFailed(InquiryMessageArrivalNotification notification, String reason) {
        notification.markFailed(reason);
        log.warn("문의 답변 알림 FCM 토큰이 없습니다: memberId={}, notificationId={}, attempts={}, shouldRetry={}, nextRetryAt={}",
                notification.getMemberId(), notification.getId(), notification.getAttempts(),
                notification.shouldRetry(), notification.getNextRetryAt());
    }
}
