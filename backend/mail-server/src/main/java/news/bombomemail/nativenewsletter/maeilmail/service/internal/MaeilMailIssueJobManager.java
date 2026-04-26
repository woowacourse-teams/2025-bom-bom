package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJob;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailIssueJobRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class MaeilMailIssueJobManager {

    private static final Long START_TRACK_ID = 0L;
    private static final int MAX_FAILED_MESSAGE_LENGTH = 1000;

    private final MaeilMailIssueJobRepository issueJobRepository;

    @Transactional
    public MaeilMailIssueJob startOrResume(LocalDate issueDate, LocalDateTime startedAt) {
        return issueJobRepository.findByIssueDate(issueDate)
                .map(issueJob -> {
                    issueJob.resume(startedAt);
                    return issueJob;
                })
                .orElseGet(() -> issueJobRepository.save(MaeilMailIssueJob.start(
                        issueDate,
                        START_TRACK_ID,
                        startedAt
                )));
    }

    @Transactional
    public void recordChunk(Long issueJobId, IssueChunkResult result) {
        MaeilMailIssueJob issueJob = getIssueJob(issueJobId);
        issueJob.recordChunk(result);
    }

    @Transactional
    public MaeilMailIssueJob complete(Long issueJobId, LocalDateTime completedAt) {
        MaeilMailIssueJob issueJob = getIssueJob(issueJobId);
        issueJob.complete(completedAt);
        return issueJob;
    }

    @Transactional
    public void fail(Long issueJobId, RuntimeException exception, LocalDateTime failedAt) {
        MaeilMailIssueJob issueJob = getIssueJob(issueJobId);
        issueJob.fail(failedMessage(exception), failedAt);
    }

    private MaeilMailIssueJob getIssueJob(Long issueJobId) {
        return issueJobRepository.findById(issueJobId)
                .orElseThrow(() -> new IllegalStateException("매일메일 발행 job을 찾을 수 없습니다. jobId=" + issueJobId));
    }

    private String failedMessage(RuntimeException exception) {
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            message = exception.getClass().getName();
        }
        if (message.length() <= MAX_FAILED_MESSAGE_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_FAILED_MESSAGE_LENGTH);
    }
}
