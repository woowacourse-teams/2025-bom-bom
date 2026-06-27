package news.bombomemail.nativenewsletter.maeilmail.scheduler;

import lombok.RequiredArgsConstructor;
import news.bombomemail.nativenewsletter.maeilmail.service.MaeilMailIssueService;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MaeilMailScheduler {

    private static final String TIME_ZONE = "Asia/Seoul";
    private static final String ISSUE_CRON = "0 0 7 * * MON-FRI";

    private final MaeilMailIssueService maeilMailIssueService;

    @Scheduled(cron = ISSUE_CRON, zone = TIME_ZONE)
    @SchedulerLock(name = "maeil_mail_issue", lockAtMostFor = "PT30M")
    public void issueMaeilMail() {
        maeilMailIssueService.issue();
    }
}
