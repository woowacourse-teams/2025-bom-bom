package news.bombomemail.nativenewsletter.maeilmail.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "maeil-mail.issue.resume-on-startup", havingValue = "true", matchIfMissing = true)
public class MaeilMailIssueStartupResumeListener {

    private final MaeilMailIssueStartupResumeExecutor startupResumeExecutor;

    @EventListener(ApplicationReadyEvent.class)
    public void resumeIncompleteMaeilMailIssueJob() {
        try {
            startupResumeExecutor.resumeIncompleteMaeilMailIssueJob();
        } catch (RuntimeException e) {
            log.error("매일메일 미완료 발행 job 자동 재개 실패", e);
        }
    }
}
