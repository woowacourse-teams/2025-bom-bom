package news.bombomemail.maildir;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DirectoryRetentionCleaner {

    private final Clock clock = Clock.systemDefaultZone();

    public CleanupResult cleanup(Path dir, int retentionDays) {
        if (!Files.isDirectory(dir)) {
            log.debug("정리 대상 디렉터리 없음: {}", dir);
            return CleanupResult.EMPTY;
        }

        Instant cutoff = Instant.now(clock).minus(retentionDays, ChronoUnit.DAYS);

        int deleted = 0;
        int failed = 0;

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path p : stream) {
                if (!Files.isRegularFile(p)) {
                    continue;
                }

                try {
                    Instant lastModified = Files.getLastModifiedTime(p).toInstant();
                    if (lastModified.isAfter(cutoff)) {
                        continue;
                    }

                    Files.deleteIfExists(p);
                    deleted++;
                } catch (Exception e) {
                    failed++;
                    log.warn("파일 삭제 실패: {}", p, e);
                }
            }
        } catch (IOException e) {
            log.error("디렉터리 조회 실패: {}", dir, e);
            return new CleanupResult(deleted, failed, true);
        }

        return new CleanupResult(deleted, failed, false);
    }

    public record CleanupResult(int deleted, int failed, boolean scanFailed) {
        public static final CleanupResult EMPTY = new CleanupResult(0, 0, false);
    }
}

