package news.bombomemail.nativenewsletter.maeilmail.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.service.internal.MaeilMailIssueChunkProcessor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MaeilMailIssueService {

    private static final Long START_TRACK_ID = 0L;

    private final Clock clock;
    private final MaeilMailIssueChunkProcessor chunkProcessor;

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
        log.info("매일메일 발행 시작 - issueDate={}, chunkSize={}", today, pageRequest.getPageSize());
        Long lastTrackId = START_TRACK_ID;
        IssueProgress progress = IssueProgress.empty();
        while (true) {
            IssueChunkResult result;
            try {
                result = chunkProcessor.process(today, lastTrackId, pageRequest);
            } catch (RuntimeException e) {
                log.error("매일메일 발행 실패 - issueDate={}, lastTrackId={}", today, lastTrackId, e);
                throw e;
            }
            if (!result.hasTracks()) {
                log.info(
                        "매일메일 발행 완료 - issueDate={}, chunkCount={}, trackCount={}, issuedArticleCount={}, previouslyIssuedTrackCount={}, elapsedMs={}",
                        today,
                        progress.chunkCount(),
                        progress.trackCount(),
                        progress.issuedArticleCount(),
                        progress.previouslyIssuedTrackCount(),
                        System.currentTimeMillis() - startedAt
                );
                return;
            }

            progress = progress.add(result);
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

    private record IssueProgress(
            int chunkCount,
            int trackCount,
            int issuedArticleCount,
            int previouslyIssuedTrackCount
    ) {

        private static IssueProgress empty() {
            return new IssueProgress(0, 0, 0, 0);
        }

        private IssueProgress add(IssueChunkResult result) {
            return new IssueProgress(
                    chunkCount + 1,
                    trackCount + result.trackCount(),
                    issuedArticleCount + result.issuedArticleCount(),
                    previouslyIssuedTrackCount + result.previouslyIssuedTrackCount()
            );
        }
    }
}
