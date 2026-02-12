package news.bombom.notification.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.Notification;
import news.bombom.notification.domain.SendResult;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.dto.NotificationResult;
import news.bombom.notification.dto.response.NotificationResultResponse;
import news.bombom.notification.service.message.NotificationMessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSenderService {

    private final NotificationService notificationService;
    private final List<NotificationMessageBuilder> messageBuilders;

    public NotificationResultResponse sendToAllDevices(Notification notification,
                                                       List<MemberFcmToken> fcmTokens) {
        int successCount = 0;
        int failCount = 0;
        int skippedCount = 0;
        StringBuilder errorMessages = new StringBuilder();

        for (MemberFcmToken fcmToken : fcmTokens) {
            SendResult result = sendToDevice(notification, fcmToken);

            switch (result.status()) {
                case SUCCESS -> successCount++;
                case FAILED -> {
                    failCount++;
                    appendErrorMessage(errorMessages, fcmToken.getDeviceUuid(), result.errorMessage());
                }
                case SKIPPED -> skippedCount++;
            }
        }

        return new NotificationResultResponse(successCount, failCount, skippedCount, errorMessages.toString());
    }

    private SendResult sendToDevice(Notification notification, MemberFcmToken fcmToken) {
        if (!fcmToken.isNotificationEnabled()) {
            log.info("알림 설정이 꺼져있어 스킵: notificationId={}, deviceUuid={}",
                    notification.getId(), fcmToken.getDeviceUuid());
            return SendResult.skipped();
        }

        try {
            NotificationMessageBuilder messageBuilder = resolveBuilder(notification);
            NotificationMessage message = messageBuilder.build(notification, fcmToken);

            NotificationResult result = notificationService.send(message);

            if (result.isSuccess()) {
                log.info("FCM 발송 성공: notificationId={}, deviceUuid={}",
                        notification.getId(), fcmToken.getDeviceUuid());
                return SendResult.success();
            } else {
                log.error("FCM 발송 실패: notificationId={}, deviceUuid={}, error={}",
                        notification.getId(), fcmToken.getDeviceUuid(), result.getErrorMessage());
                return SendResult.failed(result.getErrorMessage());
            }

        } catch (Exception e) {
            log.error("FCM 발송 실패: notificationId={}, deviceUuid={}, error={}",
                    notification.getId(), fcmToken.getDeviceUuid(), e.getMessage());
            return SendResult.failed(e.getMessage());
        }
    }

    private NotificationMessageBuilder resolveBuilder(Notification notification) {
        return messageBuilders.stream()
                .filter(b -> b.supports(notification))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(
                        "지원하지 않는 알림 타입: " + notification.getClass().getSimpleName()));
    }

    private void appendErrorMessage(StringBuilder errorMessages, String deviceUuid, String error) {
        if (!errorMessages.isEmpty()) {
            errorMessages.append("; ");
        }
        errorMessages.append("Device ").append(deviceUuid).append(": ").append(error);
    }
}
