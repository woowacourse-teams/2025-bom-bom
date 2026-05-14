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
import org.springframework.scheduling.annotation.Scheduled;

@ExtendWith(MockitoExtension.class)
class MaeilMailSchedulerTest {

    @Mock
    private MaeilMailIssueService maeilMailIssueService;

    private MaeilMailScheduler scheduler;

    @BeforeEach
    void setup() {
        scheduler = new MaeilMailScheduler(maeilMailIssueService);
    }

    @Test
    void 매일메일_발행_service를_호출한다() {
        // when
        scheduler.issueMaeilMail();

        // then
        verify(maeilMailIssueService).issue();
    }

    @Test
    void 매일메일_발행_scheduler_설정이_운영_스케줄과_락을_사용한다() throws NoSuchMethodException {
        // given
        Method method = MaeilMailScheduler.class.getDeclaredMethod("issueMaeilMail");
        Scheduled scheduled = method.getAnnotation(Scheduled.class);
        SchedulerLock schedulerLock = method.getAnnotation(SchedulerLock.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(scheduled).isNotNull();
            softly.assertThat(scheduled.cron()).isEqualTo("0 0 7 * * MON-FRI");
            softly.assertThat(scheduled.zone()).isEqualTo("Asia/Seoul");
            softly.assertThat(schedulerLock).isNotNull();
            softly.assertThat(schedulerLock.name()).isEqualTo("maeil_mail_issue");
            softly.assertThat(schedulerLock.lockAtMostFor()).isEqualTo("PT30M");
        });
    }
}
