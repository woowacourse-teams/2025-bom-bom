package me.bombom.api.v1.session.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.session.service.SessionCleanupService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 만료된 세션을 정리하는 스케줄러
 * Spring Session JDBC는 기본적으로 만료된 세션을 자동 삭제하지 않으므로
 * 주기적으로 정리해야 함
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SessionCleanupScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";

    // 매일 새벽 2시에 실행
    private static final String DAILY_CRON = "0 0 2 * * *";

    private final SessionCleanupService sessionCleanupService;

    /**
     * 매일 새벽 2시에 세션 정리
     * 혹시 놓친 만료된 세션들과 오래된 세션들을 모두 정리
     */
    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "daily_session_cleanup", lockAtLeastFor = "PT30S", lockAtMostFor = "PT10M")
    public void dailySessionCleanup() {
        log.info("일일 세션 정리 시작");
        try {
            int deletedCount = sessionCleanupService.cleanupExpiredSessionsCompletely();
            int totalSessions = sessionCleanupService.getTotalSessionCount();
            log.info("일일 세션 정리 완료 - 삭제된 세션 수: {}, 남은 세션 수: {}", deletedCount, totalSessions);
        } catch (Exception e) {
            log.error("일일 세션 정리 중 오류 발생", e);
        }
    }
}
