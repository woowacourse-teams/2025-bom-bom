package news.bombom.notification.client.firebase;

import com.google.firebase.messaging.FcmOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.client.NotificationSender;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.dto.NotificationResult;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmNotificationSender implements NotificationSender {

    private final FirebaseMessaging firebaseMessaging;

    @Override
    public NotificationResult send(NotificationMessage message) {
        try {
            Message fcmMessage = Message.builder()
                    .setToken(message.getRecipient())
                    .setNotification(createNotification(message.getTitle(), message.getContent()))
                    .putAllData(convertToStringMap(message.getData()))
                    .setFcmOptions(createFcmOptions(message.getCategory()))
                    .build();

            String response = firebaseMessaging.send(fcmMessage);
            log.info("FCM 발송 성공: recipient={}, response={}", message.getRecipient(), response);
            return NotificationResult.success(response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 실패: recipient={}, error={}", message.getRecipient(), e.getMessage());
            return NotificationResult.failure("FCM 발송 실패: " + e.getMessage(), resolveErrorCode(e));
        }
    }

    @Override
    public NotificationType getSupportedType() {
        return NotificationType.FCM;
    }

    /**
     * 토픽으로 알림 전송 (FCM 특화 기능)
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public NotificationResult sendToTopic(String topic, String title, String content, Map<String, Object> data,
                                          NotificationCategory category) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(createNotification(title, content))
                    .putAllData(convertToStringMap(data))
                    .setFcmOptions(createFcmOptions(category))
                    .build();
            String response = firebaseMessaging.send(message);
            log.info("FCM 토픽 발송 성공: topic={}, response={}", topic, response);
            return NotificationResult.success(response);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 토픽 발송 실패: topic={}, error={}", topic, e.getMessage());
            return NotificationResult.failure("FCM 토픽 발송 실패: " + e.getMessage());
        }
    }


    /**
     * 데이터만 전송 (알림 표시 없음)
     */
    public NotificationResult sendDataOnly(String token, Map<String, Object> data) {
        try {
            Message message = Message.builder()
                    .setToken(token)
                    .putAllData(convertToStringMap(data))
                    .build();

            String response = firebaseMessaging.send(message);
            log.info("FCM 데이터 전송 성공: token={}, response={}", token, response);
            return NotificationResult.success(response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 데이터 전송 실패: token={}, error={}", token, e.getMessage());
            return NotificationResult.failure("FCM 데이터 전송 실패: " + e.getMessage());
        }
    }

    private Map<String, String> convertToStringMap(Map<String, Object> data) {
        if (data == null) {
            return Map.of();
        }
        return data.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));
    }

    /**
     * Firebase 콘솔 메시지 리포트에서 카테고리별로 집계하기 위한 analytics label
     */
    private FcmOptions createFcmOptions(NotificationCategory category) {
        if (category == null) {
            return null;
        }
        try {
            return FcmOptions.withAnalyticsLabel(category.name());
        } catch (IllegalArgumentException e) {
            log.warn("FCM analytics label 형식이 유효하지 않아 라벨 없이 발송합니다: category={}", category, e);
            return null;
        }
    }

    private String resolveErrorCode(FirebaseMessagingException e) {
        if (e.getMessagingErrorCode() != null) {
            return e.getMessagingErrorCode().name();
        }
        return e.getErrorCode() != null ? e.getErrorCode().name() : null;
    }

    private Notification createNotification(String title, String content) {
        return Notification.builder()
                .setTitle(title)
                .setBody(content)
                .build();
    }
}
