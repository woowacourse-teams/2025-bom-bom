package news.bombomemail.subscribe.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnsubscribePatternReloadScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_4AM_CRON = "0 0 4 * * *";

    private final UnsubscribePatternReloadService unsubscribePatternReloadService;

    @Scheduled(cron = DAILY_4AM_CRON, zone = TIME_ZONE)
    public void reload() {
        unsubscribePatternReloadService.reload();
    }
}
