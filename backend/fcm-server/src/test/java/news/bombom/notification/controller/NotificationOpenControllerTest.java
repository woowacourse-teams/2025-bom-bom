package news.bombom.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import news.bombom.notification.domain.DevicePlatform;
import news.bombom.notification.domain.NotificationCategory;
import news.bombom.notification.service.NotificationOpenService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationOpenController.class)
@DisplayName("알림 열림 기록 컨트롤러 테스트")
class NotificationOpenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationOpenService notificationOpenService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private AuditingHandler jpaAuditingHandler;

    @Test
    @DisplayName("회원별 발송 알림의 열림을 기록한다")
    void recordOpen_MemberNotification() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/opens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category": "ARTICLE", "notificationId": 1024, "memberId": 12,
                                 "deviceUuid": "uuid", "platform": "IOS", "appVersion": "1.8.0"}
                                """))
                .andExpect(status().isNoContent());

        verify(notificationOpenService).recordOpen(argThat(request ->
                request.category() == NotificationCategory.ARTICLE
                        && request.notificationId() == 1024L
                        && request.platform() == DevicePlatform.IOS));
    }

    @Test
    @DisplayName("식별자 없는 미연결 열림도 기록한다")
    void recordOpen_Unlinked() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/opens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category": null, "platform": "ANDROID"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("EVENT 열림은 scheduleId와 함께 기록한다")
    void recordOpen_EventSchedule() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/opens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category": "EVENT", "scheduleId": 7, "memberId": 12, "platform": "ANDROID"}
                                """))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("notificationId만 있고 category가 없으면 400")
    void recordOpen_NotificationIdWithoutCategory() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/opens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"notificationId": 1024, "platform": "IOS"}
                                """))
                .andExpect(status().isBadRequest());

        verify(notificationOpenService, never()).recordOpen(any());
    }

    @Test
    @DisplayName("scheduleId를 EVENT가 아닌 category와 보내면 400")
    void recordOpen_ScheduleIdWithMemberCategory() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/opens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category": "ARTICLE", "scheduleId": 7, "platform": "IOS"}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("platform이 없으면 400")
    void recordOpen_MissingPlatform() throws Exception {
        mockMvc.perform(post("/api/v1/notifications/opens")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"category": "ARTICLE"}
                                """))
                .andExpect(status().isBadRequest());
    }
}
