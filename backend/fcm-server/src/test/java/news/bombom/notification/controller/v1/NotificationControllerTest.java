package news.bombom.notification.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import news.bombom.notification.dto.request.NotificationTokenRequest;
import news.bombom.notification.dto.request.NotificationSettingRequest;
import news.bombom.notification.dto.request.NotificationSendRequest;
import news.bombom.notification.dto.NotificationResult;
import news.bombom.notification.service.NotificationTokenService;
import news.bombom.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@DisplayName("알림 컨트롤러 v1 테스트")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationTokenService notificationTokenService;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long TEST_MEMBER_ID = 1L;
    private final String TEST_DEVICE_UUID = "test-device-uuid-123";
    private final String TEST_FCM_TOKEN = "test-fcm-token-12345";

    @Test
    @DisplayName("알림 토큰 등록 성공")
    void registerNotificationToken_Success() throws Exception {
        // Given
        NotificationTokenRequest request = new NotificationTokenRequest(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
        
        doNothing().when(notificationTokenService).registerFcmToken(anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationTokenService, times(1)).registerFcmToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
    }

    @Test
    @DisplayName("알림 토큰 등록 - 유효성 검증 실패")
    void registerNotificationToken_ValidationFailure() throws Exception {
        // Given - 잘못된 요청 (memberId가 null)
        NotificationTokenRequest invalidRequest = new NotificationTokenRequest(null, TEST_DEVICE_UUID, TEST_FCM_TOKEN);

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationTokenService, never()).registerFcmToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("알림 토큰 업서트 성공")
    void upsertNotificationToken_Success() throws Exception {
        // Given
        NotificationTokenRequest request = new NotificationTokenRequest(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
        
        doNothing().when(notificationTokenService).upsertFcmToken(anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(notificationTokenService, times(1)).upsertFcmToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
    }

    @Test
    @DisplayName("알림 토큰 업서트 - 유효성 검증 실패")
    void upsertNotificationToken_ValidationFailure() throws Exception {
        // Given - 잘못된 요청 (fcmToken이 빈 문자열)
        NotificationTokenRequest invalidRequest = new NotificationTokenRequest(TEST_MEMBER_ID, TEST_DEVICE_UUID, "");

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationTokenService, never()).upsertFcmToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("알림 설정 업데이트 성공")
    void updateNotificationSettings_Success() throws Exception {
        // Given
        NotificationSettingRequest request = new NotificationSettingRequest(true);
        
        doNothing().when(notificationTokenService).updateNotificationSetting(anyLong(), anyString(), anyBoolean());

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/tokens/{memberId}/{deviceUuid}/settings", TEST_MEMBER_ID, TEST_DEVICE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(notificationTokenService, times(1)).updateNotificationSetting(TEST_MEMBER_ID, TEST_DEVICE_UUID, true);
    }

    @Test
    @DisplayName("알림 설정 업데이트 - 잘못된 JSON")
    void updateNotificationSettings_InvalidJson() throws Exception {
        // Given - 잘못된 JSON
        String invalidJson = "{ invalid json }";

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/tokens/{memberId}/{deviceUuid}/settings", TEST_MEMBER_ID, TEST_DEVICE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());

        verify(notificationTokenService, never()).updateNotificationSetting(anyLong(), anyString(), anyBoolean());
    }

    @Test
    @DisplayName("알림 직접 발송 성공")
    void sendNotification_Success() throws Exception {
        // Given
        NotificationSendRequest request = new NotificationSendRequest(
                TEST_FCM_TOKEN, 
                "테스트 제목", 
                "테스트 내용", 
                "test-article-123"
        );
        
        NotificationResult successResult = NotificationResult.success("test-message-id");
        when(notificationService.sendNotification(anyString(), anyString(), anyString(), anyString())).thenReturn(successResult);

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).sendNotification(TEST_FCM_TOKEN, "테스트 제목", "테스트 내용", "test-article-123");
    }

    @Test
    @DisplayName("알림 직접 발송 - 유효성 검증 실패")
    void sendNotification_ValidationFailure() throws Exception {
        // Given - 잘못된 요청 (토큰이 빈 문자열)
        NotificationSendRequest invalidRequest = new NotificationSendRequest("", "테스트 제목", "테스트 내용", "test-article-123");

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationService, never()).sendNotification(anyString(), anyString(), anyString(), anyString());
    }
}
