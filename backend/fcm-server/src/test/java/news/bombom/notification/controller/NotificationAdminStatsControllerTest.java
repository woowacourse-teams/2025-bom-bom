package news.bombom.notification.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import news.bombom.notification.config.AdminApiKeyVerifier;
import news.bombom.notification.dto.response.stats.NotificationStatsResponse;
import news.bombom.notification.dto.response.stats.ReachResponse;
import news.bombom.notification.service.NotificationStatsService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.auditing.AuditingHandler;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(NotificationAdminStatsController.class)
@Import(AdminApiKeyVerifier.class)
@TestPropertySource(properties = "notification.admin.api-key=test-admin-key")
@DisplayName("어드민 알림 지표 컨트롤러 테스트")
class NotificationAdminStatsControllerTest {

    private static final String PATH = "/api/v1/notifications/admin/stats";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationStatsService notificationStatsService;

    @MockitoBean
    private JpaMetamodelMappingContext jpaMappingContext;

    @MockitoBean
    private AuditingHandler jpaAuditingHandler;

    @Test
    @DisplayName("올바른 어드민 키로 기간 지표를 조회한다")
    void getStats_Success() throws Exception {
        LocalDate from = LocalDate.of(2026, 11, 20);
        LocalDate to = LocalDate.of(2026, 11, 26);
        when(notificationStatsService.getStats(from, to)).thenReturn(new NotificationStatsResponse(
                OffsetDateTime.parse("2026-11-26T21:00:00+09:00"), from, to, List.of(),
                new ReachResponse(0, 0, 0, 0, 0), List.of(), List.of()));

        mockMvc.perform(get(PATH)
                        .header(AdminApiKeyVerifier.HEADER_NAME, "test-admin-key")
                        .param("from", "2026-11-20")
                        .param("to", "2026-11-26"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value("2026-11-20"))
                .andExpect(jsonPath("$.aggregatedAt").value("2026-11-26T21:00:00+09:00"));
    }

    @Test
    @DisplayName("어드민 키가 없으면 401")
    void getStats_MissingKey() throws Exception {
        mockMvc.perform(get(PATH).param("from", "2026-11-20").param("to", "2026-11-26"))
                .andExpect(status().isUnauthorized());

        verify(notificationStatsService, never()).getStats(any(), any());
    }

    @Test
    @DisplayName("어드민 키가 다르면 401")
    void getStats_WrongKey() throws Exception {
        mockMvc.perform(get(PATH)
                        .header(AdminApiKeyVerifier.HEADER_NAME, "wrong-key")
                        .param("from", "2026-11-20")
                        .param("to", "2026-11-26"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("기간 파라미터가 없으면 400")
    void getStats_MissingPeriod() throws Exception {
        mockMvc.perform(get(PATH).header(AdminApiKeyVerifier.HEADER_NAME, "test-admin-key"))
                .andExpect(status().isBadRequest());
    }
}
