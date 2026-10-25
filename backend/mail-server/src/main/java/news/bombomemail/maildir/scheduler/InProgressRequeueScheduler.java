package news.bombomemail.maildir.scheduler;

import jakarta.annotation.PostConstruct;
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

    @Value("${maildir.in-progress-stale-minutes:30}")
    private long staleMinutes;

    /**
     * 앱이 처리 도중 꺼지면 파일이 in-progress 에 남는다. 빈 초기화 시점은 메일 폴러가 시작되기 전이라
     * 처리 중인 파일이 없으므로, 기준 시간과 상관없이 모두 new 로 되돌린다.
     */
    @PostConstruct
    public void requeueAllOnStartup() {
        requeueModifiedBefore(Long.MAX_VALUE);
    }

    @Scheduled(fixedDelayString = "${maildir.in-progress-requeue-interval-ms:600000}")
    public void requeueStaleInProgress() {
        requeueModifiedBefore(cutoffMillis(staleMinutes));
    }

    private void requeueModifiedBefore(long cutoffMillis) {
        File inProgressDir = resolveDir(DIR_IN_PROGRESS);
        File newDir = resolveDir(DIR_NEW);

        if (!inProgressDir.isDirectory()) {
            return;
        }
        if (!ensureDirectory(newDir)) {
            return;
        }

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
