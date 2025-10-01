package me.bombom.api.v1.session.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.session.dto.SessionStatisticsResponse;
import me.bombom.api.v1.session.service.SessionCleanupService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 세션 모니터링 및 관리 컨트롤러
 * 개발/운영 환경에서 세션 상태를 모니터링하고 수동으로 정리할 수 있는 API 제공
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admin/sessions")
@Tag(name = "Session Monitoring", description = "세션 모니터링 및 관리 API")
public class SessionMonitoringController {

    private final SessionCleanupService sessionCleanupService;

    @GetMapping("/statistics")
    public ResponseEntity<SessionStatisticsResponse> getSessionStatistics() {
        int totalSessions = sessionCleanupService.getTotalSessionCount();
        int expiredSessions = sessionCleanupService.getExpiredSessionCount();
        int activeSessions = totalSessions - expiredSessions;

        SessionStatisticsResponse response = SessionStatisticsResponse.builder()
                .totalSessions(totalSessions)
                .activeSessions(activeSessions)
                .expiredSessions(expiredSessions)
                .build();

        log.info("세션 통계 조회 - 전체: {}, 활성: {}, 만료: {}", 
                totalSessions, activeSessions, expiredSessions);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cleanup")
    public ResponseEntity<String> cleanupExpiredSessions() {
        log.info("수동 세션 정리 요청");
        
        try {
            int deletedCount = sessionCleanupService.cleanupExpiredSessions();
            String message = String.format("세션 정리 완료 - 삭제된 세션 수: %d", deletedCount);
            
            log.info("수동 세션 정리 완료 - 삭제된 세션 수: {}", deletedCount);
            return ResponseEntity.ok(message);
        } catch (Exception e) {
            log.error("수동 세션 정리 중 오류 발생", e);
            return ResponseEntity.internalServerError()
                    .body("세션 정리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
}
