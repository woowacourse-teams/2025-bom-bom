package me.bombom.api.v1.reading.controller;

import static me.bombom.api.v1.common.config.ControllerTestConfig.authToken;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;
import me.bombom.api.v1.common.config.ControllerTestConfig;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.member.enums.Gender;
import me.bombom.api.v1.reading.service.MonthlyReportService;
import me.bombom.openapi.monthlyreport.model.MonthlyReportDashboardRequest;
import me.bombom.openapi.monthlyreport.model.MonthlyReportRequest;
import me.bombom.openapi.monthlyreport.model.ReadingCalendarDayResponse;
import me.bombom.openapi.monthlyreport.model.ReadingDashboardResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@WebMvcTest(controllers = MonthlyReportController.class)
@Import({MonthlyReportController.class, ControllerTestConfig.class})
class MonthlyReportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MonthlyReportService monthlyReportService;

    private Member member;
    private OAuth2AuthenticationToken authToken;

    @BeforeEach
    void setUp() {
        member = Member.builder()
                .id(1L)
                .provider("google")
                .providerId("providerId")
                .email("email@bombom.news")
                .nickname("nickname")
                .gender(Gender.FEMALE)
                .roleId(1L)
                .build();
        authToken = authToken(member);
    }

    @Test
    void 월간_읽기_캘린더를_조회한다() throws Exception {
        MonthlyReportRequest request = new MonthlyReportRequest(2026, 6);
        List<ReadingCalendarDayResponse> response = List.of(
                ReadingCalendarDayResponse.of(LocalDate.of(2026, 6, 1), true, 2)
        );
        given(monthlyReportService.getReadingCalendar(eq(member), eq(request))).willReturn(response);

        mockMvc.perform(get("/api/v1/members/me/reading/calendar")
                        .param("year", "2026")
                        .param("month", "6")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].date").value("2026-06-01"))
                .andExpect(jsonPath("$[0].read").value(true))
                .andExpect(jsonPath("$[0].readCount").value(2));

        verify(monthlyReportService).getReadingCalendar(member, request);
    }

    @Test
    void 월간_읽기_대시보드를_조회한다() throws Exception {
        MonthlyReportDashboardRequest request = new MonthlyReportDashboardRequest(2026, 6, 3);
        ReadingDashboardResponse response = ReadingDashboardResponse.of(5, null, null, 2, List.of());
        given(monthlyReportService.getReadingDashboard(eq(member), eq(request))).willReturn(response);

        mockMvc.perform(get("/api/v1/members/me/reading/dashboard")
                        .param("year", "2026")
                        .param("month", "6")
                        .param("limit", "3")
                        .with(authentication(authToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.readArticleCount").value(5))
                .andExpect(jsonPath("$.readArticleChangeRate").isEmpty())
                .andExpect(jsonPath("$.readArticleChangeDirection").isEmpty())
                .andExpect(jsonPath("$.bookmarkCount").value(2))
                .andExpect(jsonPath("$.frequentReadNewsletters").isArray());

        verify(monthlyReportService).getReadingDashboard(member, request);
    }
}
