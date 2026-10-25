package news.bombomemail.article.scheduler;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import news.bombomemail.article.service.ReceivedMailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 재처리와 재전송은 길어야 며칠 안에 끝나므로, 중복 확인 기록은 보관 기간이 지나면 지운다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ReceivedMailCleanupScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_CRON = "0 30 1 * * *";
    private static final int BATCH_SIZE = 1_000;

    private final ReceivedMailService receivedMailService;

    @Value("${mail.dedup.retention-days:7}")
    private int retentionDays;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "received_mail_cleanup", lockAtMostFor = "PT10M")
    public void cleanup() {
        if (!receivedMailService.isEnabled()) {
            return;
        }

        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int totalDeleted = 0;
        int deleted;
        do {
            deleted = receivedMailService.deleteCreatedBefore(cutoff, BATCH_SIZE);
            totalDeleted += deleted;
        } while (deleted == BATCH_SIZE);

        log.info("received_mail 정리 완료: deleted={}, cutoff={}", totalDeleted, cutoff);
    }
}
