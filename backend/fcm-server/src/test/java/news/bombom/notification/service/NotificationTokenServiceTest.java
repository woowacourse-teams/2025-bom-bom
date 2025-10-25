package news.bombom.notification.service;

import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.repository.MemberFcmTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 토큰 서비스 테스트")
class NotificationTokenServiceTest {

    @Mock
    private MemberFcmTokenRepository fcmTokenRepository;

    @InjectMocks
    private NotificationTokenService notificationTokenService;

    private final Long TEST_MEMBER_ID = 1L;
    private final String TEST_DEVICE_UUID = "test-device-uuid-123";
    private final String TEST_FCM_TOKEN = "test-fcm-token-12345";
    private final String NEW_FCM_TOKEN = "new-fcm-token-67890";

    @Test
    @DisplayName("FCM 토큰 등록 성공")
    void registerFcmToken_Success() {
        // Given
        doNothing().when(fcmTokenRepository).deleteByDeviceUuid(anyString());
        when(fcmTokenRepository.save(any(MemberFcmToken.class))).thenReturn(createTestToken());

        // When
        notificationTokenService.registerFcmToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);

        // Then
        verify(fcmTokenRepository, times(1)).deleteByDeviceUuid(TEST_DEVICE_UUID);
        verify(fcmTokenRepository, times(1)).save(any(MemberFcmToken.class));
    }

    @Test
    @DisplayName("FCM 토큰 업서트 성공 - 기존 토큰 존재")
    void upsertFcmToken_Success_ExistingToken() {
        // Given
        MemberFcmToken existingToken = mock(MemberFcmToken.class);
        when(fcmTokenRepository.findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID))
                .thenReturn(Optional.of(existingToken));
        doNothing().when(fcmTokenRepository).deleteByDeviceUuid(anyString());
        when(fcmTokenRepository.save(any(MemberFcmToken.class))).thenReturn(existingToken);

        // When
        notificationTokenService.upsertFcmToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, NEW_FCM_TOKEN);

        // Then
        verify(fcmTokenRepository, times(1)).findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID);
        verify(existingToken, times(1)).updateToken(NEW_FCM_TOKEN);
        verify(fcmTokenRepository, times(1)).deleteByDeviceUuid(TEST_DEVICE_UUID);
        verify(fcmTokenRepository, times(1)).save(any(MemberFcmToken.class));
    }

    @Test
    @DisplayName("FCM 토큰 업서트 성공 - 기존 토큰 없음")
    void upsertFcmToken_Success_NoExistingToken() {
        // Given
        when(fcmTokenRepository.findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID))
                .thenReturn(Optional.empty());
        doNothing().when(fcmTokenRepository).deleteByDeviceUuid(anyString());
        when(fcmTokenRepository.save(any(MemberFcmToken.class))).thenReturn(createTestToken());

        // When
        notificationTokenService.upsertFcmToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);

        // Then
        verify(fcmTokenRepository, times(1)).findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID);
        verify(fcmTokenRepository, times(1)).deleteByDeviceUuid(TEST_DEVICE_UUID);
        verify(fcmTokenRepository, times(1)).save(any(MemberFcmToken.class));
    }

    @Test
    @DisplayName("알림 설정 업데이트 성공")
    void updateNotificationSetting_Success() {
        // Given
        MemberFcmToken existingToken = mock(MemberFcmToken.class);
        when(fcmTokenRepository.findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID))
                .thenReturn(Optional.of(existingToken));

        // When
        notificationTokenService.updateNotificationSetting(TEST_MEMBER_ID, TEST_DEVICE_UUID, false);

        // Then
        verify(fcmTokenRepository, times(1)).findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID);
        verify(existingToken, times(1)).updateNotificationSetting(false);
    }

    @Test
    @DisplayName("알림 설정 업데이트 실패 - 토큰 없음")
    void updateNotificationSetting_Failure_TokenNotFound() {
        // Given
        when(fcmTokenRepository.findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID))
                .thenReturn(Optional.empty());

        // When
        notificationTokenService.updateNotificationSetting(TEST_MEMBER_ID, TEST_DEVICE_UUID, false);

        // Then
        verify(fcmTokenRepository, times(1)).findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID);
        // 토큰이 없으므로 updateNotificationSetting이 호출되지 않아야 함
    }

    @Test
    @DisplayName("FCM 토큰 삭제 성공")
    void unregisterFcmToken_Success() {
        // Given
        doNothing().when(fcmTokenRepository).deleteByMemberIdAndDeviceUuid(anyLong(), anyString());

        // When
        notificationTokenService.unregisterFcmToken(TEST_MEMBER_ID, TEST_DEVICE_UUID);

        // Then
        verify(fcmTokenRepository, times(1)).deleteByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID);
    }

    private MemberFcmToken createTestToken() {
        return MemberFcmToken.builder()
                .id(1L)
                .memberId(TEST_MEMBER_ID)
                .deviceUuid(TEST_DEVICE_UUID)
                .fcmToken(TEST_FCM_TOKEN)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .isNotificationEnabled(true)
                .build();
    }
}
