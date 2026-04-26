package news.bombomemail.nativenewsletter.maeilmail.service.internal;

import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJob;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJobStatus;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.repository.MaeilMailIssueJobRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueJobManagerTest {

    @Mock
    private MaeilMailIssueJobRepository issueJobRepository;

    private MaeilMailIssueJobManager issueJobManager;

    @BeforeEach
    void setup() {
        issueJobManager = new MaeilMailIssueJobManager(issueJobRepository);
    }

    @Test
    void 당일_job이_없으면_running_job을_생성한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        given(issueJobRepository.findByIssueDate(issueDate)).willReturn(Optional.empty());
        given(issueJobRepository.save(org.mockito.ArgumentMatchers.any(MaeilMailIssueJob.class)))
                .willAnswer(invocation -> invocation.getArgument(0));

        // when
        MaeilMailIssueJob issueJob = issueJobManager.startOrResume(issueDate, startedAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getIssueDate()).isEqualTo(issueDate);
            softly.assertThat(issueJob.getStatus()).isEqualTo(MaeilMailIssueJobStatus.RUNNING);
            softly.assertThat(issueJob.getLastProcessedTrackId()).isZero();
            softly.assertThat(issueJob.getStartedAt()).isEqualTo(startedAt);
        });
    }

    @Test
    void 실패한_job이_있으면_cursor를_유지하고_resume한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, startedAt);
        issueJob.recordChunk(IssueChunkResult.of(10L, 2, 1, 0));
        issueJob.fail("fail", startedAt.plusMinutes(10));
        given(issueJobRepository.findByIssueDate(issueDate)).willReturn(Optional.of(issueJob));

        // when
        MaeilMailIssueJob result = issueJobManager.startOrResume(issueDate, startedAt.plusMinutes(20));

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getStatus()).isEqualTo(MaeilMailIssueJobStatus.RUNNING);
            softly.assertThat(result.getLastProcessedTrackId()).isEqualTo(10L);
            softly.assertThat(result.getFailedMessage()).isNull();
            softly.assertThat(result.getFailedAt()).isNull();
        });
    }

    @Test
    void chunk_결과를_job에_기록한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, startedAt);
        IssueChunkResult chunkResult = IssueChunkResult.of(10L, 2, 1, 0);
        given(issueJobRepository.findById(1L)).willReturn(Optional.of(issueJob));

        // when
        issueJobManager.recordChunk(1L, chunkResult);

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getLastProcessedTrackId()).isEqualTo(10L);
            softly.assertThat(issueJob.getChunkCount()).isEqualTo(1);
            softly.assertThat(issueJob.getProcessedTrackCount()).isEqualTo(2);
            softly.assertThat(issueJob.getIssuedArticleCount()).isEqualTo(1);
        });
    }

    @Test
    void 실패시_failed_상태와_실패메시지를_기록한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime failedAt = LocalDateTime.of(2026, 4, 27, 7, 10);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, LocalDateTime.of(2026, 4, 27, 7, 0));
        RuntimeException exception = new RuntimeException("발행 실패");
        given(issueJobRepository.findById(1L)).willReturn(Optional.of(issueJob));

        // when
        issueJobManager.fail(1L, exception, failedAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getStatus()).isEqualTo(MaeilMailIssueJobStatus.FAILED);
            softly.assertThat(issueJob.getFailedMessage()).isEqualTo("발행 실패");
            softly.assertThat(issueJob.getFailedAt()).isEqualTo(failedAt);
        });
    }

    @Test
    void 완료시_completed_상태로_변경한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime completedAt = LocalDateTime.of(2026, 4, 27, 7, 10);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, LocalDateTime.of(2026, 4, 27, 7, 0));
        given(issueJobRepository.findById(1L)).willReturn(Optional.of(issueJob));

        // when
        MaeilMailIssueJob result = issueJobManager.complete(1L, completedAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(result.getStatus()).isEqualTo(MaeilMailIssueJobStatus.COMPLETED);
            softly.assertThat(result.getCompletedAt()).isEqualTo(completedAt);
        });
        verify(issueJobRepository).findById(1L);
    }

    private MaeilMailIssueJob issueJob(Long id, LocalDate issueDate, LocalDateTime startedAt) {
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(issueDate, 0L, startedAt);
        ReflectionTestUtils.setField(issueJob, "id", id);
        return issueJob;
    }
}
