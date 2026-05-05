package news.bombomemail.subscribe.alert;

import lombok.RequiredArgsConstructor;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import news.bombomemail.subscribe.alert.service.UnsubscribeUrlAlertService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UnsubscribeUrlAlertScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_CRON = "0 0 13 * * *"; // 오후 1시

    private final UnsubscribeUrlAlertService unsubscribeUrlAlertService;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "unsubscribe_url_alert", lockAtMostFor = "PT5M")
    public void sendAlert() {
        unsubscribeUrlAlertService.drainAndSend();
    }
}
