package news.bombom.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.dto.request.NotificationCategorySettingRequest;
import news.bombom.notification.dto.response.NotificationCategorySettingResponse;
import news.bombom.notification.service.NotificationSettingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationSettingController.class)
@DisplayName("알림 설정 컨트롤러 테스트")
class NotificationSettingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationSettingService notificationSettingService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private AuditingHandler jpaAuditingHandler;

    @Autowired
    private ObjectMapper objectMapper;

    private final Long TEST_MEMBER_ID = 1L;

    @Test
    @DisplayName("사용자의 전체 알림 설정 조회 성공")
    void getCategorySettings_Success() throws Exception {
        // Given
        List<NotificationCategorySettingResponse> responses = List.of(
                new NotificationCategorySettingResponse(NotificationCategory.ARTICLE, true),
                new NotificationCategorySettingResponse(NotificationCategory.EVENT, false));
        when(notificationSettingService.getCategorySettings(TEST_MEMBER_ID)).thenReturn(responses);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/{memberId}/settings", TEST_MEMBER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].category").value("ARTICLE"))
                .andExpect(jsonPath("$[0].enabled").value(true));
    }

    @ParameterizedTest
    @CsvSource({
            "challenge-todo-reminder, CHALLENGE_TODO_REMINDER",
            "challenge_todo_reminder, CHALLENGE_TODO_REMINDER",
            "aRtIcLe, ARTICLE"
    })
    @DisplayName("특정 카테고리 알림 설정 조회 시 category path variable이 enum으로 변환된다")
    void getCategorySetting_CategoryPathVariableConverted(String pathCategory, NotificationCategory expectedCategory) throws Exception {
        // Given
        NotificationCategorySettingResponse response = new NotificationCategorySettingResponse(expectedCategory, true);
        when(notificationSettingService.getCategorySetting(eq(TEST_MEMBER_ID), eq(expectedCategory))).thenReturn(response);

        // When & Then
        mockMvc.perform(get("/api/v1/notifications/{memberId}/settings/{category}", TEST_MEMBER_ID, pathCategory))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.category").value(expectedCategory.name()))
                .andExpect(jsonPath("$.enabled").value(true));
    }

    @Test
    @DisplayName("특정 카테고리 알림 설정 업데이트 성공")
    void updateNotificationCategorySetting_Success() throws Exception {
        // Given
        NotificationCategorySettingRequest request = new NotificationCategorySettingRequest(false);
        doNothing().when(notificationSettingService).updateCategorySetting(anyLong(), any(), anyBoolean());

        // When & Then
        mockMvc.perform(patch("/api/v1/notifications/{memberId}/settings/{category}",
                TEST_MEMBER_ID, NotificationCategory.ARTICLE)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());
    }
}
