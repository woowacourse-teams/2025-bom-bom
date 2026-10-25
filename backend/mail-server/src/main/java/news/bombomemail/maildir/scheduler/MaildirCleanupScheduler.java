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

    @Value("${maildir.failed-retention-days:15}")
    private int failedRetentionDays;

    @Value("${maildir.discarded-retention-days:15}")
    private int discardedRetentionDays;

    @Value("${maildir.duplicate-retention-days:15}")
    private int duplicateRetentionDays;

    @Scheduled(cron = DAILY_CRON, zone = TIME_ZONE)
    public void cleanupMaildir() {
        cleanup("processed", processedRetentionDays);
        cleanup("parsing-failed", failedRetentionDays);
        cleanup("discarded", discardedRetentionDays);
        cleanup("duplicate", duplicateRetentionDays);
    }

    private void cleanup(String dirName, int retentionDays) {
        Path dir = Paths.get(baseDir, dirName);

        DirectoryRetentionCleaner.CleanupResult result = cleaner.cleanup(dir, retentionDays);

        if (result.scanFailed()) {
            log.error("{} 정리 실패(스캔 실패): dir={}, deleted={}, failed={}",
                    dirName, dir, result.deleted(), result.failed());
            return;
        }

        if (result.deleted() > 0 || result.failed() > 0) {
            log.info("{} 정리 완료: dir={}, deleted={}, failed={}, retentionDays={}",
                    dirName, dir, result.deleted(), result.failed(), retentionDays);
        } else {
            log.debug("{} 정리 대상 없음: dir={}, retentionDays={}", dirName, dir, retentionDays);
        }
    }
}
