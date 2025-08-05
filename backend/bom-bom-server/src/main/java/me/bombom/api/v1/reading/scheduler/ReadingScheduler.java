package me.bombom.api.v1.reading.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReadingScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_CRON = "0 0 0 * * *";
    private static final String WEEKLY_CRON = "0 0 0 * * MON";

    private final ReadingService readingService;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    public void daily() {
        log.info("오늘 읽기 초기화 실행");
        readingService.resetContinueReadingCount();
        readingService.resetTodayReadingCount();
    }

    @Scheduled(cron = WEEKLY_CRON, zone = TIME_ZONE)
    public void weekly() {
        log.info("주간 읽기 초기화 실행");
        readingService.resetWeeklyReadingCount();
    }
}
