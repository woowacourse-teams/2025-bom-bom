package news.bombomemail.maildir.scheduler;

import java.nio.file.Path;
import java.nio.file.Paths;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.maildir.DirectoryRetentionCleaner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class MaildirCleanupScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String DAILY_CRON = "0 0 1 * * *";

    private final DirectoryRetentionCleaner cleaner;

    @Value("${maildir.base-dir}")
    private String baseDir;

    @Value("${maildir.processed-retention-days:5}")
    private int processedRetentionDays;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    public void cleanupProcessed() {
        Path processedDir = Paths.get(baseDir, "processed");

        DirectoryRetentionCleaner.CleanupResult result = cleaner.cleanup(processedDir, processedRetentionDays);

        if (result.scanFailed()) {
            log.error("processed 정리 실패(스캔 실패): dir={}, deleted={}, failed={}",
                    processedDir, result.deleted(), result.failed());
            return;
        }

        if (result.deleted() > 0 || result.failed() > 0) {
            log.info("processed 정리 완료: dir={}, deleted={}, failed={}, retentionDays={}",
                    processedDir, result.deleted(), result.failed(), processedRetentionDays);
        } else {
            log.debug("processed 정리 대상 없음: dir={}, retentionDays={}", processedDir, processedRetentionDays);
        }
    }
}
