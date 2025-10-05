package me.bombom.api.v1.session.controller;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.session.dto.SessionCleanupResponse;
import me.bombom.api.v1.session.dto.SessionStatisticsResponse;
import me.bombom.api.v1.session.service.SessionMonitoringService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 세션 모니터링 및 관리 컨트롤러
 * HTTP 요청/응답 처리만 담당하고 비즈니스 로직은 SessionMonitoringService에 위임
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/sessions")
public class SessionController {

    private final SessionMonitoringService sessionMonitoringService;

    @GetMapping("/statistics")
    public SessionStatisticsResponse getSessionStatistics() {
        return sessionMonitoringService.getSessionStatistics();
    }

    @PostMapping("/cleanup")
    public SessionCleanupResponse cleanupExpiredSessions() {
        return sessionMonitoringService.cleanupExpiredSessions();
    }
}
