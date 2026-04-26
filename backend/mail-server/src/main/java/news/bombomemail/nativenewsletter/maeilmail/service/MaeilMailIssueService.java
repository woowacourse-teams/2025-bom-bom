package news.bombomemail.nativenewsletter.maeilmail.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJob;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.service.internal.MaeilMailIssueChunkProcessor;
import news.bombomemail.nativenewsletter.maeilmail.service.internal.MaeilMailIssueJobManager;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaeilMailIssueService {

    private final Clock clock;
    private final MaeilMailIssueChunkProcessor chunkProcessor;
    private final MaeilMailIssueJobManager issueJobManager;

    @Value("${maeil-mail.issue.chunk-size:200}")
    private int issueChunkSize;

    public void issue() {
        long startedAt = System.currentTimeMillis();
        LocalDate today = LocalDate.now(clock);
        if (isWeekend(today)) {
            log.info("매일메일 발행 스킵 - issueDate={}, reason=weekend", today);
            return;
        }

        PageRequest pageRequest = issuePageRequest();
        MaeilMailIssueJob issueJob = issueJobManager.startOrResume(today, LocalDateTime.now(clock));
        if (issueJob.isCompleted()) {
            log.info("매일메일 발행 스킵 - issueDate={}, issueJobId={}, reason=already_completed", today, issueJob.getId());
            return;
        }

        log.info(
                "매일메일 발행 시작 - issueDate={}, issueJobId={}, chunkSize={}, lastProcessedTrackId={}",
                today,
                issueJob.getId(),
                pageRequest.getPageSize(),
                issueJob.getLastProcessedTrackId()
        );
        Long lastTrackId = issueJob.getLastProcessedTrackId();
        while (true) {
            IssueChunkResult result;
            try {
                result = chunkProcessor.process(issueJob.getId(), today, lastTrackId, pageRequest);
            } catch (RuntimeException e) {
                issueJobManager.fail(issueJob.getId(), e, LocalDateTime.now(clock));
                log.error("매일메일 발행 실패 - issueDate={}, issueJobId={}, lastTrackId={}", today, issueJob.getId(), lastTrackId, e);
                throw e;
            }
            if (!result.hasTracks()) {
                MaeilMailIssueJob completedJob = issueJobManager.complete(issueJob.getId(), LocalDateTime.now(clock));
                log.info(
                        "매일메일 발행 완료 - issueDate={}, issueJobId={}, chunkCount={}, trackCount={}, issuedArticleCount={}, previouslyIssuedTrackCount={}, elapsedMs={}",
                        today,
                        completedJob.getId(),
                        completedJob.getChunkCount(),
                        completedJob.getProcessedTrackCount(),
                        completedJob.getIssuedArticleCount(),
                        completedJob.getPreviouslyIssuedTrackCount(),
                        System.currentTimeMillis() - startedAt
                );
                return;
            }

            lastTrackId = result.lastTrackId();
        }
    }

    private PageRequest issuePageRequest() {
        return PageRequest.of(0, Math.max(issueChunkSize, 1));
    }

    private boolean isWeekend(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }
}
