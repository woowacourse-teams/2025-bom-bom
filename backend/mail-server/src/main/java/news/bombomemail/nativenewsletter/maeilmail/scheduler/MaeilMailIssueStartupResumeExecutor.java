package news.bombomemail.nativenewsletter.maeilmail.scheduler;

import lombok.RequiredArgsConstructor;
import news.bombomemail.nativenewsletter.maeilmail.service.MaeilMailIssueService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueStartupResumeExecutor {

    private final MaeilMailIssueService maeilMailIssueService;

    @SchedulerLock(name = "maeil_mail_issue", lockAtMostFor = "PT30M")
    public void resumeIncompleteMaeilMailIssueJob() {
        maeilMailIssueService.resumeIncompleteTodayJob();
    }
}
