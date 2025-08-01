package me.bombom.api.v1.reading.scheduler;

import lombok.RequiredArgsConstructor;
import me.bombom.api.v1.reading.service.ReadingService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReadingScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";

    private final ReadingService readingService;

    @Scheduled(cron="0 0 5 * * *", zone = TIME_ZONE)
    public void daily() {
        readingService.resetTodayReadingCount();
    }

    @Scheduled(cron="0 0 5 * * MON", zone = TIME_ZONE)
    public void weekly() {
        readingService.resetWeeklyReadingCount();
    }
}
