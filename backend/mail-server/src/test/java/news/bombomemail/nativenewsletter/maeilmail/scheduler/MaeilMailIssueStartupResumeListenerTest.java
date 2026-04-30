package news.bombomemail.nativenewsletter.maeilmail.scheduler;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doThrow;

import java.lang.reflect.Method;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.event.EventListener;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueStartupResumeListenerTest {

    @Mock
    private MaeilMailIssueStartupResumeExecutor startupResumeExecutor;

    private MaeilMailIssueStartupResumeListener listener;

    @BeforeEach
    void setup() {
        listener = new MaeilMailIssueStartupResumeListener(startupResumeExecutor);
    }

    @Test
    void 서버_시작시_미완료_매일메일_발행_job_재개를_시도한다() {
        // when
        listener.resumeIncompleteMaeilMailIssueJob();

        // then
        verify(startupResumeExecutor).resumeIncompleteMaeilMailIssueJob();
    }

    @Test
    void 서버_시작시_자동_재개에_실패해도_예외를_전파하지_않는다() {
        // given
        doThrow(new RuntimeException("fail")).when(startupResumeExecutor).resumeIncompleteMaeilMailIssueJob();

        // when & then
        assertThatCode(() -> listener.resumeIncompleteMaeilMailIssueJob())
                .doesNotThrowAnyException();
    }

    @Test
    void 서버_시작_재개_listener는_설정_없이도_활성화된다() throws NoSuchMethodException {
        // given
        ConditionalOnProperty conditionalOnProperty = MaeilMailIssueStartupResumeListener.class
                .getAnnotation(ConditionalOnProperty.class);
        Method method = MaeilMailIssueStartupResumeListener.class
                .getDeclaredMethod("resumeIncompleteMaeilMailIssueJob");
        EventListener eventListener = method.getAnnotation(EventListener.class);

        // then
        assertSoftly(softly -> {
            softly.assertThat(conditionalOnProperty).isNotNull();
            softly.assertThat(conditionalOnProperty.name()).containsExactly("maeil-mail.issue.resume-on-startup");
            softly.assertThat(conditionalOnProperty.havingValue()).isEqualTo("true");
            softly.assertThat(conditionalOnProperty.matchIfMissing()).isTrue();
            softly.assertThat(eventListener).isNotNull();
        });
    }
}
