package news.bombom.notification.service;

import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombom.notification.client.firebase.FcmNotificationSender;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.dto.NotificationResult;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final FcmNotificationSender fcmNotificationSender;

    /**
     * 개별 토큰으로 알림 전송 (비즈니스 로직 포함)
     */
    public NotificationResult sendNotification(String token, String title, String body, String articleId) {
        // 비즈니스 로직: 토큰 유효성 검증, 알림 설정 확인 등
        if (!StringUtils.hasText(token)) {
            return NotificationResult.failure("FCM 토큰이 유효하지 않습니다.");
        }

        if (!StringUtils.hasText(title)) {
            return NotificationResult.failure("알림 제목이 필요합니다.");
        }

        if (!StringUtils.hasText(body)) {
            return NotificationResult.failure("알림 내용이 필요합니다.");
        }

        log.info("FCM 알림 전송 시작: title={}, body={}, articleId={}", title, body, articleId);

        NotificationMessage message = NotificationMessage.builder()
                .recipient(token)
                .title(title)
                .content(body)
                .type(NotificationType.FCM)
                .data(Map.of(
                        "articleId", articleId,
                        "notificationType", "arrivedArticle"
                ))
                .build();

        return fcmNotificationSender.send(message);
    }

    /**
     * 여러 토큰으로 일괄 알림 전송 (비즈니스 로직 포함)
     */
    public Map<String, NotificationResult> sendBulkNotification(List<String> tokens, String title, String body,
                                                                String articleId) {
        if (tokens == null || tokens.isEmpty()) {
            throw new IllegalArgumentException("전송할 토큰 목록이 비어있습니다.");
        }

        log.info("FCM 일괄 알림 전송 시작: 총 {}개 토큰, title={}, articleId={}", tokens.size(), title, articleId);

        Map<String, NotificationResult> results = new java.util.HashMap<>();
        int successCount = 0;

        for (String token : tokens) {
            NotificationResult result = sendNotification(token, title, body, articleId);
            results.put(token, result);
            if (result.isSuccess()) {
                successCount++;
            }
        }

        log.info("FCM 일괄 발송 완료: 총 {}개 중 {}개 성공", tokens.size(), successCount);

        return results;
    }

    /**
     * 토픽으로 알림 전송 (비즈니스 로직 포함)
     */
    public NotificationResult sendNotificationToTopic(String topic, String title, String body, String articleId) {
        if (!StringUtils.hasText(topic)) {
            return NotificationResult.failure("토픽명이 유효하지 않습니다.");
        }

        log.info("FCM 토픽 알림 전송 시작: topic={}, title={}, articleId={}", topic, title, articleId);

        Map<String, Object> data = Map.of(
                "articleId", articleId,
                "notificationType", "topicNotification"
        );

        return fcmNotificationSender.sendToTopic(topic, title, body, data);
    }

    /**
     * 데이터만 전송 (알림 표시 없음) - 비즈니스 로직 포함
     */
    public NotificationResult sendDataOnly(String token, Map<String, Object> data) {
        if (!StringUtils.hasText(token)) {
            return NotificationResult.failure("FCM 토큰이 유효하지 않습니다.");
        }

        if (data == null || data.isEmpty()) {
            return NotificationResult.failure("전송할 데이터가 비어있습니다.");
        }

        log.info("FCM 데이터 전송 시작: token={}, dataSize={}", token, data.size());

        return fcmNotificationSender.sendDataOnly(token, data);
    }

    /**
     * 알림 전송 결과 검증 및 로깅
     */
    public boolean validateNotificationResult(NotificationResult result) {
        if (result == null) {
            log.warn("FCM 응답이 비어있습니다.");
            return false;
        }

        if (result.isSuccess()) {
            log.info("FCM 전송 결과 검증 성공: {}", result.getMessageId());
            return true;
        } else {
            log.warn("FCM 전송 결과 검증 실패: {}", result.getErrorMessage());
            return false;
        }
    }
}
