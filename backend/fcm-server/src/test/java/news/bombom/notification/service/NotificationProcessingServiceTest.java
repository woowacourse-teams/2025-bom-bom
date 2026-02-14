package news.bombom.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import news.bombom.notification.domain.ArticleArrivalNotification;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.response.NotificationResultResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 처리 서비스 테스트")
class NotificationProcessingServiceTest {

    @Mock
    private NotificationTokenService notificationTokenService;

    @Mock
    private NotificationSenderService notificationSender;

    @Mock
    private NotificationStatusService statusUpdater;

    @Mock
    private NotificationSettingService notificationSettingService;

    @InjectMocks
    private NotificationProcessingService notificationProcessingService;

    private final Long TEST_MEMBER_ID = 1L;

    @Test
    @DisplayName("아티클 알림 설정이 비활성화되어 있으면 전송하지 않고 로그를 남긴다")
    void processArticleArrivedNotification_SettingsDisabled_DoesNotSend() {
        // Given
        ArticleArrivalNotification notification = createNotification();
        when(notificationSettingService.isEnabled(TEST_MEMBER_ID, NotificationCategory.ARTICLE))
                .thenReturn(false);

        // When
        notificationProcessingService.processArticleArrivedNotification(notification);

        // Then
        verify(notificationSettingService, times(1)).isEnabled(TEST_MEMBER_ID, NotificationCategory.ARTICLE);
        verify(notificationTokenService, never()).resolveTokens(anyLong());
        verify(notificationSender, never()).sendToAllDevices(any(), anyList());
        verify(statusUpdater, never()).updateStatus(any(), any());
    }

    @Test
    @DisplayName("아티클 알림 설정이 활성화되어 있고 토큰이 있으면 전송한다")
    void processArticleArrivedNotification_SettingsEnabledAndTokensExist_SendsNotification() {
        // Given
        ArticleArrivalNotification notification = createNotification();
        List<MemberFcmToken> tokens = List.of(MemberFcmToken.builder()
                .memberId(TEST_MEMBER_ID)
                .deviceUuid("test-device")
                .fcmToken("test-token")
                .isNotificationEnabled(true)
                .build());

        when(notificationSettingService.isEnabled(TEST_MEMBER_ID, NotificationCategory.ARTICLE))
                .thenReturn(true);
        when(notificationTokenService.resolveTokens(TEST_MEMBER_ID))
                .thenReturn(tokens);
        when(notificationSender.sendToAllDevices(eq(notification), anyList()))
                .thenReturn(new NotificationResultResponse(1, 0, 0, ""));

        // When
        notificationProcessingService.processArticleArrivedNotification(notification);

        // Then
        verify(notificationSettingService, times(1)).isEnabled(TEST_MEMBER_ID, NotificationCategory.ARTICLE);
        verify(notificationTokenService, times(1)).resolveTokens(TEST_MEMBER_ID);
        verify(notificationSender, times(1)).sendToAllDevices(eq(notification), anyList());
        verify(statusUpdater, times(1)).updateStatus(eq(notification), any());
    }

    @Test
    @DisplayName("아티클 알림 설정은 활성화되어 있으나 토큰이 없으면 실패 처리한다")
    void processArticleArrivedNotification_SettingsEnabledButNoTokens_MarksAsFailed() {
        // Given
        ArticleArrivalNotification notification = createNotification();

        when(notificationSettingService.isEnabled(TEST_MEMBER_ID, NotificationCategory.ARTICLE))
                .thenReturn(true);
        when(notificationTokenService.resolveTokens(TEST_MEMBER_ID))
                .thenReturn(Collections.emptyList());

        // When
        notificationProcessingService.processArticleArrivedNotification(notification);

        // Then
        verify(notificationSettingService, times(1)).isEnabled(TEST_MEMBER_ID, NotificationCategory.ARTICLE);
        verify(statusUpdater, times(1)).markAsFailed(eq(notification), anyString());
        verify(notificationSender, never()).sendToAllDevices(any(), anyList());
    }

    @Test
    @DisplayName("알림 토큰 등록 시 토큰 저장과 설정 서비스가 모두 호출된다")
    void registerToken_CallsBothServices() {
        // Given
        String deviceUuid = "device-123";
        String token = "token-123";

        // When
        notificationProcessingService.registerToken(TEST_MEMBER_ID, deviceUuid, token);

        // Then
        verify(notificationTokenService, times(1)).registerToken(TEST_MEMBER_ID, deviceUuid, token);
        verify(notificationSettingService, times(1)).ensureMemberNotificationSetting(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("알림 토큰 업데이트 시 토큰 처리와 설정 서비스가 모두 호출된다")
    void upsertToken_CallsBothServices() {
        // Given
        String deviceUuid = "device-123";
        String token = "token-123";

        // When
        notificationProcessingService.upsertToken(TEST_MEMBER_ID, deviceUuid, token);

        // Then
        verify(notificationTokenService, times(1)).upsertToken(TEST_MEMBER_ID, deviceUuid, token);
        verify(notificationSettingService, times(1)).ensureMemberNotificationSetting(TEST_MEMBER_ID);
    }

    private ArticleArrivalNotification createNotification() {
        return ArticleArrivalNotification.builder()
                .memberId(TEST_MEMBER_ID)
                .articleId(123L)
                .articleTitle("테스트 제목")
                .newsletterName("테스트 뉴스레터")
                .build();
    }
}
