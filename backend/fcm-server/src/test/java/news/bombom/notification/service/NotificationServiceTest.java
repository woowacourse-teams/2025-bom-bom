package news.bombom.notification.service;

import news.bombom.notification.client.firebase.FcmNotificationSender;
import news.bombom.notification.domain.NotificationType;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.dto.NotificationResult;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 서비스 테스트")
class NotificationServiceTest {

    @Mock
    private FcmNotificationSender fcmNotificationSender;

    @InjectMocks
    private NotificationService notificationService;

    private final String TEST_TOKEN = "test-fcm-token-12345";
    private final String TEST_TITLE = "테스트 제목";
    private final String TEST_BODY = "테스트 내용";
    private final String TEST_ARTICLE_ID = "test-article-123";

    @Test
    @DisplayName("개별 알림 전송 성공")
    void sendNotification_Success() {
        // Given
        NotificationResult expectedResult = NotificationResult.success("test-message-id");
        when(fcmNotificationSender.send(any(NotificationMessage.class))).thenReturn(expectedResult);

        // When
        NotificationResult result = notificationService.sendNotification(TEST_TOKEN, TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isEqualTo("test-message-id");
        
        verify(fcmNotificationSender, times(1)).send(any(NotificationMessage.class));
    }

    @Test
    @DisplayName("개별 알림 전송 실패 - 토큰이 null")
    void sendNotification_Failure_NullToken() {
        // When
        NotificationResult result = notificationService.sendNotification(null, TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("FCM 토큰이 유효하지 않습니다");
        
        verify(fcmNotificationSender, never()).send(any(NotificationMessage.class));
    }

    @Test
    @DisplayName("개별 알림 전송 실패 - 토큰이 빈 문자열")
    void sendNotification_Failure_EmptyToken() {
        // When
        NotificationResult result = notificationService.sendNotification("", TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("FCM 토큰이 유효하지 않습니다");
        
        verify(fcmNotificationSender, never()).send(any(NotificationMessage.class));
    }

    @Test
    @DisplayName("개별 알림 전송 실패 - 제목이 null")
    void sendNotification_Failure_NullTitle() {
        // When
        NotificationResult result = notificationService.sendNotification(TEST_TOKEN, null, TEST_BODY, TEST_ARTICLE_ID);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("알림 제목이 필요합니다");
        
        verify(fcmNotificationSender, never()).send(any(NotificationMessage.class));
    }

    @Test
    @DisplayName("개별 알림 전송 실패 - 내용이 null")
    void sendNotification_Failure_NullBody() {
        // When
        NotificationResult result = notificationService.sendNotification(TEST_TOKEN, TEST_TITLE, null, TEST_ARTICLE_ID);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("알림 내용이 필요합니다");
        
        verify(fcmNotificationSender, never()).send(any(NotificationMessage.class));
    }

    @Test
    @DisplayName("일괄 알림 전송 성공")
    void sendBulkNotification_Success() {
        // Given
        List<String> tokens = List.of(TEST_TOKEN, "token2", "token3");
        NotificationResult successResult = NotificationResult.success("test-message-id");
        when(fcmNotificationSender.send(any(NotificationMessage.class))).thenReturn(successResult);

        // When
        Map<String, NotificationResult> results = notificationService.sendBulkNotification(tokens, TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID);

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.values()).allMatch(NotificationResult::isSuccess);
        
        verify(fcmNotificationSender, times(3)).send(any(NotificationMessage.class));
    }

    @Test
    @DisplayName("일괄 알림 전송 실패 - 토큰 목록이 null")
    void sendBulkNotification_Failure_NullTokens() {
        // When & Then
        assertThatThrownBy(() -> notificationService.sendBulkNotification(null, TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전송할 토큰 목록이 비어있습니다");
    }

    @Test
    @DisplayName("일괄 알림 전송 실패 - 토큰 목록이 비어있음")
    void sendBulkNotification_Failure_EmptyTokens() {
        // When & Then
        assertThatThrownBy(() -> notificationService.sendBulkNotification(List.of(), TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("전송할 토큰 목록이 비어있습니다");
    }

    @Test
    @DisplayName("토픽 알림 전송 성공")
    void sendNotificationToTopic_Success() {
        // Given
        String topic = "test-topic";
        NotificationResult expectedResult = NotificationResult.success("test-message-id");
        when(fcmNotificationSender.sendToTopic(anyString(), anyString(), anyString(), anyMap())).thenReturn(expectedResult);

        // When
        NotificationResult result = notificationService.sendNotificationToTopic(topic, TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isEqualTo("test-message-id");
        
        verify(fcmNotificationSender, times(1)).sendToTopic(eq(topic), eq(TEST_TITLE), eq(TEST_BODY), anyMap());
    }

    @Test
    @DisplayName("토픽 알림 전송 실패 - 토픽이 null")
    void sendNotificationToTopic_Failure_NullTopic() {
        // When
        NotificationResult result = notificationService.sendNotificationToTopic(null, TEST_TITLE, TEST_BODY, TEST_ARTICLE_ID);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("토픽명이 유효하지 않습니다");
        
        verify(fcmNotificationSender, never()).sendToTopic(anyString(), anyString(), anyString(), anyMap());
    }

    @Test
    @DisplayName("데이터만 전송 성공")
    void sendDataOnly_Success() {
        // Given
        Map<String, Object> data = Map.of("key1", "value1", "key2", "value2");
        NotificationResult expectedResult = NotificationResult.success("test-message-id");
        when(fcmNotificationSender.sendDataOnly(anyString(), anyMap())).thenReturn(expectedResult);

        // When
        NotificationResult result = notificationService.sendDataOnly(TEST_TOKEN, data);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getMessageId()).isEqualTo("test-message-id");
        
        verify(fcmNotificationSender, times(1)).sendDataOnly(TEST_TOKEN, data);
    }

    @Test
    @DisplayName("데이터만 전송 실패 - 토큰이 null")
    void sendDataOnly_Failure_NullToken() {
        // Given
        Map<String, Object> data = Map.of("key1", "value1");

        // When
        NotificationResult result = notificationService.sendDataOnly(null, data);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("FCM 토큰이 유효하지 않습니다");
        
        verify(fcmNotificationSender, never()).sendDataOnly(anyString(), anyMap());
    }

    @Test
    @DisplayName("데이터만 전송 실패 - 데이터가 null")
    void sendDataOnly_Failure_NullData() {
        // When
        NotificationResult result = notificationService.sendDataOnly(TEST_TOKEN, null);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("전송할 데이터가 비어있습니다");
        
        verify(fcmNotificationSender, never()).sendDataOnly(anyString(), anyMap());
    }

    @Test
    @DisplayName("알림 결과 검증 성공")
    void validateNotificationResult_Success() {
        // Given
        NotificationResult successResult = NotificationResult.success("test-message-id");

        // When
        boolean isValid = notificationService.validateNotificationResult(successResult);

        // Then
        assertThat(isValid).isTrue();
    }

    @Test
    @DisplayName("알림 결과 검증 실패 - 결과가 null")
    void validateNotificationResult_Failure_NullResult() {
        // When
        boolean isValid = notificationService.validateNotificationResult(null);

        // Then
        assertThat(isValid).isFalse();
    }

    @Test
    @DisplayName("알림 결과 검증 실패 - 실패한 결과")
    void validateNotificationResult_Failure_FailedResult() {
        // Given
        NotificationResult failedResult = NotificationResult.failure("test-error");

        // When
        boolean isValid = notificationService.validateNotificationResult(failedResult);

        // Then
        assertThat(isValid).isFalse();
    }
}
