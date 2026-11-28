package news.bombom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import news.bombom.article.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.domain.NotificationDeviceSendLog;
import news.bombom.notification.domain.SendResult.SendStatus;
import news.bombom.notification.dto.NotificationMessage;
import news.bombom.notification.dto.NotificationResult;
import news.bombom.notification.dto.response.NotificationResultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("알림 기기별 발송 서비스 테스트")
class NotificationSenderServiceTest {

    private NotificationService notificationService;
    private NotificationMessageBuilder messageBuilder;
    private NotificationDeviceSendLogService deviceSendLogService;
    private NotificationSenderService senderService;

    private ArticleArrivalNotification notification;

    @BeforeEach
    void setUp() {
        notificationService = mock(NotificationService.class);
        messageBuilder = mock(NotificationMessageBuilder.class);
        deviceSendLogService = mock(NotificationDeviceSendLogService.class);
        senderService = new NotificationSenderService(notificationService, List.of(messageBuilder), deviceSendLogService);

        notification = ArticleArrivalNotification.builder()
                .memberId(1L)
                .articleId(10L)
                .newsletterName("봄봄레터")
                .articleTitle("아티클 제목")
                .build();
        ReflectionTestUtils.setField(notification, "id", 1024L);

        when(messageBuilder.supports(any())).thenReturn(true);
        when(messageBuilder.getCategory()).thenReturn(NotificationCategory.ARTICLE);
        when(messageBuilder.build(any(), any())).thenReturn(NotificationMessage.builder().recipient("token").build());
    }

    @Test
    @SuppressWarnings("unchecked")
    void 기기별_발송_결과를_카테고리_알림id_에러코드와_함께_기록한다() {
        MemberFcmToken successDevice = createToken("device-success", true);
        MemberFcmToken failedDevice = createToken("device-failed", true);
        MemberFcmToken skippedDevice = createToken("device-skipped", false);
        when(notificationService.send(any()))
                .thenReturn(NotificationResult.success("message-id"))
                .thenReturn(NotificationResult.failure("FCM 발송 실패", "UNREGISTERED"));

        NotificationResultResponse result = senderService.sendToAllDevices(
                notification, List.of(successDevice, failedDevice, skippedDevice));

        assertThat(result.successCount()).isEqualTo(1);
        assertThat(result.failCount()).isEqualTo(1);
        assertThat(result.skippedCount()).isEqualTo(1);

        ArgumentCaptor<List<NotificationDeviceSendLog>> captor = ArgumentCaptor.forClass(List.class);
        verify(deviceSendLogService).recordAll(captor.capture());
        List<NotificationDeviceSendLog> logs = captor.getValue();
        assertThat(logs).extracting(NotificationDeviceSendLog::getStatus)
                .containsExactly(SendStatus.SUCCESS, SendStatus.FAILED, SendStatus.SKIPPED);
        assertThat(logs).extracting(NotificationDeviceSendLog::getErrorCode)
                .containsExactly(null, "UNREGISTERED", null);
        assertThat(logs).allSatisfy(log -> {
            assertThat(log.getCategory()).isEqualTo(NotificationCategory.ARTICLE);
            assertThat(log.getNotificationId()).isEqualTo(1024L);
            assertThat(log.getMemberId()).isEqualTo(1L);
        });
    }

    @Test
    void 기기별_발송_로그_저장이_실패해도_발송_결과를_반환한다() {
        when(notificationService.send(any())).thenReturn(NotificationResult.success("message-id"));
        doThrow(new RuntimeException("DB 오류")).when(deviceSendLogService).recordAll(anyList());

        NotificationResultResponse result = senderService.sendToAllDevices(
                notification, List.of(createToken("device", true)));

        assertThat(result.successCount()).isEqualTo(1);
    }

    private MemberFcmToken createToken(String deviceUuid, boolean enabled) {
        return MemberFcmToken.builder()
                .memberId(1L)
                .deviceUuid(deviceUuid)
                .fcmToken("fcm-" + deviceUuid)
                .isNotificationEnabled(enabled)
                .build();
    }
}
