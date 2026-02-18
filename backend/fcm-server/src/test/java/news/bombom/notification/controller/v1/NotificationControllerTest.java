package news.bombom.notification.controller.v1;

import com.fasterxml.jackson.databind.ObjectMapper;
import news.bombom.notification.controller.NotificationController;
import news.bombom.notification.dto.request.NotificationTokenRequest;
import news.bombom.notification.dto.request.NotificationSettingRequest;
import news.bombom.notification.dto.request.NotificationSendRequest;
import news.bombom.notification.dto.NotificationResult;
import news.bombom.notification.service.NotificationProcessingService;
import news.bombom.notification.service.NotificationTokenService;
import news.bombom.notification.service.NotificationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

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
    private NotificationProcessingService notificationProcessingService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private AuditingHandler jpaAuditingHandler;

    @MockitoBean
    private NotificationService notificationService;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long TEST_MEMBER_ID = 1L;
    private final String TEST_DEVICE_UUID = "test-device-uuid-123";
    private final String TEST_FCM_TOKEN = "test-fcm-token-12345";

    @Test
    @DisplayName("알림 토큰 등록 성공")
    void registerToken_Success() throws Exception {
        // Given
        NotificationTokenRequest request = new NotificationTokenRequest(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
        doNothing().when(notificationProcessingService).registerToken(anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        verify(notificationProcessingService, times(1)).registerToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
    }

    @Test
    @DisplayName("알림 토큰 등록 - 유효성 검증 실패")
    void registerToken_ValidationFailure() throws Exception {
        // Given - 잘못된 요청 (memberId가 null)
        NotificationTokenRequest invalidRequest = new NotificationTokenRequest(null, TEST_DEVICE_UUID,
                TEST_FCM_TOKEN);

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());

        verify(notificationProcessingService, never()).registerToken(anyLong(), anyString(), anyString());
    }

    @Test
    @DisplayName("알림 토큰 업데이트 성공")
    void updateToken_Success() throws Exception {
        // Given
        NotificationTokenRequest request = new NotificationTokenRequest(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
        doNothing().when(notificationProcessingService).upsertToken(anyLong(), anyString(), anyString());

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/tokens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(notificationProcessingService, times(1)).upsertToken(TEST_MEMBER_ID, TEST_DEVICE_UUID, TEST_FCM_TOKEN);
    }

    @Test
    @DisplayName("기기별 알림 설정 업데이트 성공")
    void updateDeviceNotificationSettings_Success() throws Exception {
        // Given
        NotificationSettingRequest request = new NotificationSettingRequest(true);
        doNothing().when(notificationTokenService).updateNotificationSetting(anyLong(), anyString(),
                anyBoolean());

        // When & Then
        mockMvc.perform(put("/api/v1/notifications/tokens/{memberId}/{deviceUuid}/settings", TEST_MEMBER_ID, TEST_DEVICE_UUID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        verify(notificationTokenService, times(1)).updateNotificationSetting(TEST_MEMBER_ID, TEST_DEVICE_UUID, true);
    }

    @Test
    @DisplayName("알림 직접 발송 성공")
    void sendNotification_Success() throws Exception {
        // Given
        NotificationSendRequest request = new NotificationSendRequest(
                TEST_FCM_TOKEN,
                "테스트 제목",
                "테스트 내용",
                Map.of("articleId", "test-article-123")
        );

        NotificationResult successResult = NotificationResult.success("test-message-id");
        when(notificationService.sendNotification(anyString(), anyString(), anyString(), anyMap()))
                .thenReturn(successResult);

        // When & Then
        mockMvc.perform(post("/api/v1/notifications/send")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        verify(notificationService, times(1)).sendNotification(
                eq(TEST_FCM_TOKEN),
                eq("테스트 제목"),
                eq("테스트 내용"),
                eq(Map.of("articleId", "test-article-123"))
        );
    }
}
