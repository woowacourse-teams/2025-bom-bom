package me.bombom.api.v1.common.holiday.scheduler;

import java.time.Clock;
import java.time.Year;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.bombom.api.v1.common.DiscordWebhookNotifier;
import me.bombom.api.v1.common.holiday.service.HolidaySyncService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidaySyncScheduler {

    private static final String SEOUL_TIMEZONE = "Asia/Seoul";

    private final Clock clock;
    private final HolidaySyncService holidaySyncService;
    private final DiscordWebhookNotifier discordWebhookNotifier;

    /**
     * 연중 발표되는 임시공휴일도 반영되도록 올해와 내년 공휴일을 주기적으로 동기화한다.
     */
    @Scheduled(cron = "${holiday.api.cron}", zone = SEOUL_TIMEZONE)
    @SchedulerLock(name = "sync_holidays", lockAtLeastFor = "PT4S", lockAtMostFor = "PT1M")
    public void syncHolidays() {
        log.info("공휴일 동기화 시작");
        Year thisYear = Year.now(clock);
        for (Year year : List.of(thisYear, thisYear.plusYears(1))) {
            try {
                holidaySyncService.sync(year);
            } catch (Exception e) {
                log.error("{}년 공휴일 동기화에 실패했습니다.", year, e);
                discordWebhookNotifier.sendHolidaySyncErrorNotification(year, e);
            }
        }
    }
}
