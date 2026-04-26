package news.bombomemail.nativenewsletter.maeilmail.domain;

import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalDateTime;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import org.junit.jupiter.api.Test;

class MaeilMailIssueJobTest {

    @Test
    void chunk_기록시_cursor와_발행_count를_누적한다() {
        // given
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(
                LocalDate.of(2026, 4, 27),
                0L,
                LocalDateTime.of(2026, 4, 27, 7, 0)
        );

        // when
        issueJob.recordChunk(IssueChunkResult.of(10L, 2, 1, 0));
        issueJob.recordChunk(IssueChunkResult.of(20L, 3, 2, 1));

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getStatus()).isEqualTo(MaeilMailIssueJobStatus.RUNNING);
            softly.assertThat(issueJob.getLastProcessedTrackId()).isEqualTo(20L);
            softly.assertThat(issueJob.getChunkCount()).isEqualTo(2);
            softly.assertThat(issueJob.getProcessedTrackCount()).isEqualTo(5);
            softly.assertThat(issueJob.getIssuedArticleCount()).isEqualTo(3);
            softly.assertThat(issueJob.getPreviouslyIssuedTrackCount()).isEqualTo(1);
        });
    }

    @Test
    void 실패한_job을_resume하면_cursor를_유지하고_running으로_돌린다() {
        // given
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(
                LocalDate.of(2026, 4, 27),
                0L,
                LocalDateTime.of(2026, 4, 27, 7, 0)
        );
        issueJob.recordChunk(IssueChunkResult.of(10L, 2, 1, 0));
        issueJob.fail("fail", LocalDateTime.of(2026, 4, 27, 7, 10));

        // when
        issueJob.resume(LocalDateTime.of(2026, 4, 27, 7, 20));

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getStatus()).isEqualTo(MaeilMailIssueJobStatus.RUNNING);
            softly.assertThat(issueJob.getLastProcessedTrackId()).isEqualTo(10L);
            softly.assertThat(issueJob.getFailedMessage()).isNull();
            softly.assertThat(issueJob.getFailedAt()).isNull();
        });
    }

    @Test
    void complete하면_완료상태와_완료시각을_기록한다() {
        // given
        LocalDateTime completedAt = LocalDateTime.of(2026, 4, 27, 7, 30);
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(
                LocalDate.of(2026, 4, 27),
                0L,
                LocalDateTime.of(2026, 4, 27, 7, 0)
        );

        // when
        issueJob.complete(completedAt);

        // then
        assertSoftly(softly -> {
            softly.assertThat(issueJob.getStatus()).isEqualTo(MaeilMailIssueJobStatus.COMPLETED);
            softly.assertThat(issueJob.getCompletedAt()).isEqualTo(completedAt);
            softly.assertThat(issueJob.isCompleted()).isTrue();
        });
    }
}
