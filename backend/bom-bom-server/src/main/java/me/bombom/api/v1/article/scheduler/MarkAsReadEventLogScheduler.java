package me.bombom.api.v1.article.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.article.service.MarkAsReadEventLogService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MarkAsReadEventLogScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_3_30_AM_CRON = "0 30 3 * * *";

    private final MarkAsReadEventLogService markAsReadEventLogService;

    @Scheduled(cron = DAILY_3_30_AM_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "cleanup_old_mark_as_read_event_logs", lockAtLeastFor = "PT4S", lockAtMostFor = "PT30S")
    public void cleanupOldLogs() {
        markAsReadEventLogService.cleanupOldLogs();
    }
}
