package me.bombom.api.v1.reading.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.common.resolver.LoginMember;
import me.bombom.api.v1.member.domain.Member;
import me.bombom.api.v1.reading.service.MonthlyReportService;
import me.bombom.openapi.api.MonthlyReportApi;
import me.bombom.openapi.model.MonthlyReportDashboardRequest;
import me.bombom.openapi.model.MonthlyReportRequest;
import me.bombom.openapi.model.ReadingCalendarDayResponse;
import me.bombom.openapi.model.ReadingDashboardResponse;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class MonthlyReportController implements MonthlyReportApi {

    private final MonthlyReportService monthlyReportService;

    @Override
    public List<ReadingCalendarDayResponse> getReadingCalendar(
            @LoginMember Member member,
            @Valid @ModelAttribute MonthlyReportRequest request
    ) {
        return monthlyReportService.getReadingCalendar(member, request);
    }

    @Override
    public ReadingDashboardResponse getReadingDashboard(
            @LoginMember Member member,
            @Valid @ModelAttribute MonthlyReportDashboardRequest request
    ) {
        return monthlyReportService.getReadingDashboard(member, request);
    }
}
