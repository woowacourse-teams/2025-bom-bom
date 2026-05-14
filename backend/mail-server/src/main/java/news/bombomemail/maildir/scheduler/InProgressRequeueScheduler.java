package news.bombomemail.maildir.scheduler;

import java.io.File;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InProgressRequeueScheduler {

    private static final String DIR_IN_PROGRESS = "in-progress";
    private static final String DIR_NEW = "new";
    private static final long MILLIS_PER_MINUTE = 60_000L;

    @Value("${maildir.base-dir}")
    private String baseDir;

    @Value("${maildir.in-progress-stale-minutes:60}")
    private long staleMinutes;

    @Scheduled(fixedDelayString = "${maildir.in-progress-requeue-interval-ms:1800000}")
    public void requeueStaleInProgress() {
        File inProgressDir = resolveDir(DIR_IN_PROGRESS);
        File newDir = resolveDir(DIR_NEW);

        if (!inProgressDir.isDirectory()) {
            return;
        }
        if (!ensureDirectory(newDir)) {
            return;
        }

        long cutoffMillis = cutoffMillis(staleMinutes);

        File[] files = inProgressDir.listFiles(File::isFile);
        if (files == null) {
            return;
        }

        for (File file : files) {
            if (!isStale(file, cutoffMillis)) {
                continue;
            }

            File target = new File(newDir, file.getName());
            if (!file.renameTo(target)) {
                log.warn("stale requeue 실패: {}", file.getAbsolutePath());
                continue;
            }
            log.warn("stale requeue: {}", target.getAbsolutePath());
        }
    }

    private File resolveDir(String dirName) {
        return new File(baseDir, dirName);
    }

    private boolean ensureDirectory(File dir) {
        return dir.exists() || dir.mkdirs();
    }

    private long cutoffMillis(long staleMinutes) {
        return System.currentTimeMillis() - (staleMinutes * MILLIS_PER_MINUTE);
    }

    private boolean isStale(File file, long cutoffMillis) {
        // 마지막 수정 시간이 cutoff보다 이전이면 stale로 판단
        return file.lastModified() < cutoffMillis;
    }
}
