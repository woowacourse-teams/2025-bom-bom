package news.bombom.notification.client.firebase;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.dto.NotificationResult;
import news.bombom.notification.client.NotificationSender;
import news.bombom.notification.domain.NotificationType;
import org.springframework.stereotype.Component;

import java.util.Map;

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
                    .setNotification(Notification.builder()
                            .setTitle(message.getTitle())
                            .setBody(message.getContent())
                            .build())
                    .putAllData(convertToStringMap(message.getData()))
                    .build();

            String response = firebaseMessaging.send(fcmMessage);
            log.info("FCM 발송 성공: recipient={}, response={}", message.getRecipient(), response);
            return NotificationResult.success(response);

        } catch (FirebaseMessagingException e) {
            log.error("FCM 발송 실패: recipient={}, error={}", message.getRecipient(), e.getMessage());
            return NotificationResult.failure("FCM 발송 실패: " + e.getMessage());
        }
    }

    @Override
    public NotificationType getSupportedType() {
        return NotificationType.FCM;
    }

    /**
     * 토픽으로 알림 전송 (FCM 특화 기능)
     */
    public NotificationResult sendToTopic(String topic, String title, String content, Map<String, Object> data) {
        try {
            Message message = Message.builder()
                    .setTopic(topic)
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(content)
                            .build())
                    .putAllData(convertToStringMap(data))
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
                .collect(java.util.stream.Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> String.valueOf(entry.getValue())
                ));
    }
}
