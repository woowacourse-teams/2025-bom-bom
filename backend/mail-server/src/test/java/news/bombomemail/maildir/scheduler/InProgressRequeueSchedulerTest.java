package news.bombomemail.maildir.scheduler;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.test.util.ReflectionTestUtils;

class InProgressRequeueSchedulerTest {

    private static final long STALE_MINUTES = 30;

    @TempDir
    Path baseDir;

    InProgressRequeueScheduler scheduler;

    @BeforeEach
    void setUp() {
        scheduler = new InProgressRequeueScheduler();
        ReflectionTestUtils.setField(scheduler, "baseDir", baseDir.toString());
        ReflectionTestUtils.setField(scheduler, "staleMinutes", STALE_MINUTES);
    }

    @Test
    void 기준_시간이_지난_파일만_new로_되돌린다() throws IOException {
        // given
        createInProgressFile("stale.eml", Duration.ofMinutes(STALE_MINUTES + 1));
        createInProgressFile("fresh.eml", Duration.ofMinutes(1));

        // when
        scheduler.requeueStaleInProgress();

        // then
        assertThat(baseDir.resolve("new").resolve("stale.eml")).exists();
        assertThat(baseDir.resolve("in-progress").resolve("fresh.eml")).exists();
    }

    @Test
    void 앱이_시작될_때는_기준_시간과_상관없이_모두_new로_되돌린다() throws IOException {
        // given
        createInProgressFile("stale.eml", Duration.ofMinutes(STALE_MINUTES + 1));
        createInProgressFile("fresh.eml", Duration.ZERO);

        // when
        scheduler.requeueAllOnStartup();

        // then
        assertThat(baseDir.resolve("new").resolve("stale.eml")).exists();
        assertThat(baseDir.resolve("new").resolve("fresh.eml")).exists();
        assertThat(baseDir.resolve("in-progress")).isEmptyDirectory();
    }

    @Test
    void in_progress_폴더가_없으면_아무것도_하지_않는다() {
        // when
        scheduler.requeueAllOnStartup();

        // then
        assertThat(baseDir.resolve("new")).doesNotExist();
    }

    private void createInProgressFile(String fileName, Duration age) throws IOException {
        Path inProgressDir = Files.createDirectories(baseDir.resolve("in-progress"));
        Path file = Files.writeString(inProgressDir.resolve(fileName), "Subject: test\r\n\r\nbody");
        Files.setLastModifiedTime(file, FileTime.from(Instant.now().minus(age)));
    }
}
