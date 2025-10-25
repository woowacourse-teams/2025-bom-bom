package news.bombom.notification.config;

import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.MockedStatic;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("알림 설정 테스트")
class NotificationConfigTest {

    @Mock
    private ClassPathResource classPathResource;

    @Mock
    private InputStream inputStream;

    @InjectMocks
    private NotificationConfig notificationConfig;

    @Test
    @DisplayName("FirebaseMessaging 빈 생성 성공 - 정상적인 서비스 계정 키")
    void firebaseMessaging_Success() throws IOException {
        // Given
        String serviceAccountKeyPath = "key/bombom-fcm.json";
        ReflectionTestUtils.setField(notificationConfig, "serviceAccountKeyPath", serviceAccountKeyPath);

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            firebaseAppMock.when(() -> FirebaseApp.getApps()).thenReturn(List.of());
            firebaseAppMock.when(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class))).thenReturn(mock(FirebaseApp.class));

            try (MockedStatic<FirebaseMessaging> firebaseMessagingMock = mockStatic(FirebaseMessaging.class)) {
                FirebaseMessaging mockFirebaseMessaging = mock(FirebaseMessaging.class);
                firebaseMessagingMock.when(FirebaseMessaging::getInstance).thenReturn(mockFirebaseMessaging);

                // When
                FirebaseMessaging result = notificationConfig.firebaseMessaging();

                // Then
                assertThat(result).isNotNull();
                firebaseAppMock.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), times(1));
                firebaseMessagingMock.verify(FirebaseMessaging::getInstance, times(1));
            }
        }
    }

    @Test
    @DisplayName("FirebaseMessaging 빈 생성 성공 - 테스트 모드 (빈 서비스 계정 키)")
    void firebaseMessaging_Success_TestMode() throws IOException {
        // Given
        String serviceAccountKeyPath = "";
        ReflectionTestUtils.setField(notificationConfig, "serviceAccountKeyPath", serviceAccountKeyPath);

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            firebaseAppMock.when(() -> FirebaseApp.getApps()).thenReturn(List.of());

            // When
            FirebaseMessaging result = notificationConfig.firebaseMessaging();

            // Then
            assertThat(result).isNull();
            firebaseAppMock.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), never());
        }
    }

    @Test
    @DisplayName("FirebaseMessaging 빈 생성 성공 - 이미 초기화된 앱")
    void firebaseMessaging_Success_AlreadyInitialized() throws IOException {
        // Given
        String serviceAccountKeyPath = "key/bombom-fcm.json";
        ReflectionTestUtils.setField(notificationConfig, "serviceAccountKeyPath", serviceAccountKeyPath);

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            FirebaseApp mockApp = mock(FirebaseApp.class);
            firebaseAppMock.when(() -> FirebaseApp.getApps()).thenReturn(List.of(mockApp));

            try (MockedStatic<FirebaseMessaging> firebaseMessagingMock = mockStatic(FirebaseMessaging.class)) {
                FirebaseMessaging mockFirebaseMessaging = mock(FirebaseMessaging.class);
                firebaseMessagingMock.when(FirebaseMessaging::getInstance).thenReturn(mockFirebaseMessaging);

                // When
                FirebaseMessaging result = notificationConfig.firebaseMessaging();

                // Then
                assertThat(result).isNotNull();
                firebaseAppMock.verify(() -> FirebaseApp.initializeApp(any(FirebaseOptions.class)), never());
                firebaseMessagingMock.verify(FirebaseMessaging::getInstance, times(1));
            }
        }
    }

    @Test
    @DisplayName("FirebaseMessaging 빈 생성 실패 - IOException")
    void firebaseMessaging_Failure_IOException() throws IOException {
        // Given
        String serviceAccountKeyPath = "invalid/path.json";
        ReflectionTestUtils.setField(notificationConfig, "serviceAccountKeyPath", serviceAccountKeyPath);

        try (MockedStatic<FirebaseApp> firebaseAppMock = mockStatic(FirebaseApp.class)) {
            firebaseAppMock.when(() -> FirebaseApp.getApps()).thenReturn(List.of());

            // When & Then
            assertThatThrownBy(() -> notificationConfig.firebaseMessaging())
                    .isInstanceOf(IOException.class);
        }
    }
}
