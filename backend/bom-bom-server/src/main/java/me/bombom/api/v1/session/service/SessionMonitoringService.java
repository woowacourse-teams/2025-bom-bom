package me.bombom.api.v1.session.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.exception.CServerErrorException;
import me.bombom.api.v1.common.exception.ErrorDetail;
import me.bombom.api.v1.session.dto.SessionCleanupResponse;
import me.bombom.api.v1.session.dto.SessionStatisticsResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 세션 모니터링 서비스
 * 세션 통계 조회 및 정리 작업의 비즈니스 로직 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SessionMonitoringService {

    private final SessionCleanupService sessionCleanupService;

    /**
     * 세션 통계 정보를 조회하고 응답 DTO로 변환
     * 
     * @return 세션 통계 응답
     */
    public SessionStatisticsResponse getSessionStatistics() {
        try {
            int totalSessions = sessionCleanupService.getTotalSessionCount();
            int expiredSessions = sessionCleanupService.getExpiredSessionCount();
            int activeSessions = totalSessions - expiredSessions;
            SessionStatisticsResponse response = SessionStatisticsResponse.of(totalSessions, expiredSessions, activeSessions);

            log.info("세션 통계 조회 - 전체: {}, 활성: {}, 만료: {}", 
                    totalSessions, activeSessions, expiredSessions);

            return response;
        } catch (Exception e) {
            log.error("세션 통계 조회 중 오류 발생", e);
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("operation", "getSessionStatistics")
                    .addContext("errorMessage", e.getMessage());
        }
    }

    /**
     * 만료된 세션을 정리하고 결과를 응답 DTO로 변환
     * 
     * @return 세션 정리 결과 응답
     */
    public SessionCleanupResponse cleanupExpiredSessions() {
        log.info("수동 세션 정리 요청");
        
        try {
            int deletedCount = sessionCleanupService.cleanupExpiredSessions();
            String message = String.format("세션 정리 완료 - 삭제된 세션 수: %d", deletedCount);
            
            log.info("수동 세션 정리 완료 - 삭제된 세션 수: {}", deletedCount);
            
            return SessionCleanupResponse.of(deletedCount, message);
        } catch (Exception e) {
            log.error("수동 세션 정리 중 오류 발생", e);
            throw new CServerErrorException(ErrorDetail.INTERNAL_SERVER_ERROR)
                    .addContext("operation", "cleanupExpiredSessions")
                    .addContext("errorMessage", e.getMessage());
        }
    }
}
