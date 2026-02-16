package news.bombom.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import news.bombom.notification.domain.MemberFcmToken;
import news.bombom.notification.repository.MemberFcmTokenRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    @DisplayName("토큰 등록 성공")
    void registerToken_Success() {
        // Given
        doNothing().when(fcmTokenRepository).deleteByDeviceUuid(anyString());
        when(fcmTokenRepository.save(any(MemberFcmToken.class))).thenReturn(createTestToken());

        // When
        notificationTokenService.registerToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);

        // Then
        verify(fcmTokenRepository, times(1)).deleteByDeviceUuid(TEST_DEVICE_UUID);
        verify(fcmTokenRepository, times(1)).save(any(MemberFcmToken.class));
    }

    @Test
    @DisplayName("토큰 업데이트 성공 - 기존 토큰 존재")
    void upsertToken_Success_ExistingToken() {
        // Given
        MemberFcmToken existingToken = mock(MemberFcmToken.class);
        when(fcmTokenRepository.findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID))
                .thenReturn(Optional.of(existingToken));

        // When
        notificationTokenService.upsertToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, NEW_FCM_TOKEN);

        // Then
        verify(fcmTokenRepository, times(1)).findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID);
        verify(existingToken, times(1)).updateToken(NEW_FCM_TOKEN);
    }

    @Test
    @DisplayName("토큰 업데이트 성공 - 기존 토큰 없음")
    void upsertToken_Success_NoExistingToken() {
        // Given
        when(fcmTokenRepository.findByMemberIdAndDeviceUuid(TEST_MEMBER_ID, TEST_DEVICE_UUID))
                .thenReturn(Optional.empty());
        doNothing().when(fcmTokenRepository).deleteByDeviceUuid(anyString());
        when(fcmTokenRepository.save(any(MemberFcmToken.class)))
                .thenReturn(createTestToken());

        // When
        notificationTokenService.upsertToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);

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

    @Test
    @DisplayName("회원의 토큰 리스트 반환")
    void resolveTokens_ReturnsTokens() {
        // Given
        when(fcmTokenRepository.findByMemberId(TEST_MEMBER_ID)).thenReturn(List.of(createTestToken()));

        // When
        List<MemberFcmToken> result = notificationTokenService.resolveTokens(TEST_MEMBER_ID);

        // Then
        assertThat(result).hasSize(1);
        verify(fcmTokenRepository, times(1)).findByMemberId(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("토큰이 없으면 빈 목록 반환")
    void resolveTokens_NoTokens_ReturnsEmpty() {
        // Given
        when(fcmTokenRepository.findByMemberId(TEST_MEMBER_ID)).thenReturn(List.of());

        // When
        List<MemberFcmToken> result = notificationTokenService.resolveTokens(TEST_MEMBER_ID);

        // Then
        assertThat(result).isEmpty();
        verify(fcmTokenRepository, times(1)).findByMemberId(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("getTokenStrings - 토큰 문자열 리스트 반환")
    void getTokenStrings_ReturnsTokenStrings() {
        // Given
        when(fcmTokenRepository.findByMemberId(TEST_MEMBER_ID)).thenReturn(List.of(createTestToken()));

        // When
        List<String> result = notificationTokenService.getTokenStrings(TEST_MEMBER_ID);

        // Then
        assertThat(result).containsExactly(TEST_FCM_TOKEN);
        verify(fcmTokenRepository, times(1)).findByMemberId(TEST_MEMBER_ID);
    }

    @Test
    @DisplayName("getTokenStrings - 토큰 없으면 빈 리스트 반환")
    void getTokenStrings_NoTokens_ReturnsEmptyList() {
        // Given
        when(fcmTokenRepository.findByMemberId(TEST_MEMBER_ID)).thenReturn(List.of());

        // When
        List<String> result = notificationTokenService.getTokenStrings(TEST_MEMBER_ID);

        // Then
        assertThat(result).isEmpty();
        verify(fcmTokenRepository, times(1)).findByMemberId(TEST_MEMBER_ID);
    }

    private MemberFcmToken createTestToken() {
        return MemberFcmToken.builder()
                .id(1L)
                .memberId(TEST_MEMBER_ID)
                .deviceUuid(TEST_DEVICE_UUID)
                .fcmToken(TEST_FCM_TOKEN)
                .isNotificationEnabled(true)
                .build();
    }
}
