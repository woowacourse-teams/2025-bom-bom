package news.bombomemail.nativenewsletter.maeilmail.scheduler;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.Mockito.verify;

import java.lang.reflect.Method;
import news.bombomemail.nativenewsletter.maeilmail.service.MaeilMailIssueService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueStartupResumeExecutorTest {

    @Mock
    private MaeilMailIssueService maeilMailIssueService;

    private MaeilMailIssueStartupResumeExecutor startupResumeExecutor;

    @BeforeEach
    void setup() {
        startupResumeExecutor = new MaeilMailIssueStartupResumeExecutor(maeilMailIssueService);
    }

    @Test
    void 미완료_매일메일_발행_job_재개를_실행한다() {
        // when
        startupResumeExecutor.resumeIncompleteMaeilMailIssueJob();

        // then
        verify(maeilMailIssueService).resumeIncompleteTodayJob();
    }

    @Test
    void 서버_시작_재개_executor는_스케줄러와_같은_락을_사용한다() throws NoSuchMethodException {
        // given
        Method method = MaeilMailIssueStartupResumeExecutor.class
                .getDeclaredMethod("resumeIncompleteMaeilMailIssueJob");
        SchedulerLock schedulerLock = method.getAnnotation(SchedulerLock.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(schedulerLock).isNotNull();
            softly.assertThat(schedulerLock.name()).isEqualTo("maeil_mail_issue");
            softly.assertThat(schedulerLock.lockAtMostFor()).isEqualTo("PT30M");
        });
    }
}
