package news.bombomemail.nativenewsletter.maeilmail.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import news.bombomemail.nativenewsletter.maeilmail.domain.MaeilMailIssueJob;
import news.bombomemail.nativenewsletter.maeilmail.dto.IssueChunkResult;
import news.bombomemail.nativenewsletter.maeilmail.service.internal.MaeilMailIssueChunkProcessor;
import news.bombomemail.nativenewsletter.maeilmail.service.internal.MaeilMailIssueJobManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class MaeilMailIssueServiceTest {

    private static final ZoneId SEOUL = ZoneId.of("Asia/Seoul");

    @Mock
    private MaeilMailIssueChunkProcessor chunkProcessor;

    @Mock
    private MaeilMailIssueJobManager issueJobManager;

    private MaeilMailIssueService issueService;

    @BeforeEach
    void setup() {
        issueService = new MaeilMailIssueService(
                clockAt(LocalDateTime.of(2026, 4, 27, 7, 0)),
                chunkProcessor,
                issueJobManager
        );
        ReflectionTestUtils.setField(issueService, "issueChunkSize", 200);
    }

    @Test
    void 주말이면_발행하지_않는다() {
        // given
        MaeilMailIssueService weekendIssueService = new MaeilMailIssueService(
                clockAt(LocalDateTime.of(2026, 4, 26, 7, 0)),
                chunkProcessor,
                issueJobManager
        );

        // when
        weekendIssueService.issue();

        // then
        verifyNoInteractions(chunkProcessor, issueJobManager);
    }

    @Test
    void 평일이면_chunk가_없을_때까지_순차적으로_처리한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, 0L, startedAt);
        PageRequest pageRequest = PageRequest.of(0, 200);
        given(issueJobManager.startOrResume(issueDate, startedAt)).willReturn(issueJob);
        given(chunkProcessor.process(1L, issueDate, 0L, pageRequest))
                .willReturn(IssueChunkResult.of(10L, 2, 1, 0));
        given(chunkProcessor.process(1L, issueDate, 10L, pageRequest))
                .willReturn(IssueChunkResult.empty());
        given(issueJobManager.complete(1L, startedAt)).willReturn(issueJob);

        // when
        issueService.issue();

        // then
        verify(chunkProcessor).process(1L, issueDate, 0L, pageRequest);
        verify(chunkProcessor).process(1L, issueDate, 10L, pageRequest);
        verify(issueJobManager).complete(1L, startedAt);
    }

    @Test
    void chunkSize가_1보다_작으면_1로_보정한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, 0L, startedAt);
        PageRequest pageRequest = PageRequest.of(0, 1);
        ReflectionTestUtils.setField(issueService, "issueChunkSize", 0);
        given(issueJobManager.startOrResume(issueDate, startedAt)).willReturn(issueJob);
        given(chunkProcessor.process(1L, issueDate, 0L, pageRequest))
                .willReturn(IssueChunkResult.empty());
        given(issueJobManager.complete(1L, startedAt)).willReturn(issueJob);

        // when
        issueService.issue();

        // then
        verify(chunkProcessor).process(1L, issueDate, 0L, pageRequest);
    }

    @Test
    void 완료된_job이면_발행하지_않는다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, 0L, startedAt);
        issueJob.complete(startedAt);
        given(issueJobManager.startOrResume(issueDate, startedAt)).willReturn(issueJob);

        // when
        issueService.issue();

        // then
        verifyNoInteractions(chunkProcessor);
    }

    @Test
    void 기존_job의_cursor부터_chunk를_이어간다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, 10L, startedAt);
        PageRequest pageRequest = PageRequest.of(0, 200);
        given(issueJobManager.startOrResume(issueDate, startedAt)).willReturn(issueJob);
        given(chunkProcessor.process(1L, issueDate, 10L, pageRequest))
                .willReturn(IssueChunkResult.empty());
        given(issueJobManager.complete(1L, startedAt)).willReturn(issueJob);

        // when
        issueService.issue();

        // then
        verify(chunkProcessor).process(1L, issueDate, 10L, pageRequest);
    }

    @Test
    void chunk_처리_실패시_job을_failed로_변경한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, 0L, startedAt);
        RuntimeException exception = new RuntimeException("fail");
        PageRequest pageRequest = PageRequest.of(0, 200);
        given(issueJobManager.startOrResume(issueDate, startedAt)).willReturn(issueJob);
        given(chunkProcessor.process(1L, issueDate, 0L, pageRequest)).willThrow(exception);

        // when & then
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> issueService.issue())
                .isSameAs(exception);
        verify(issueJobManager).fail(1L, exception, startedAt);
    }

    @Test
    void 미완료_job이_있으면_cursor부터_재개한다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        MaeilMailIssueJob issueJob = issueJob(1L, issueDate, 10L, startedAt);
        PageRequest pageRequest = PageRequest.of(0, 200);
        given(issueJobManager.resumeIncomplete(issueDate, startedAt)).willReturn(Optional.of(issueJob));
        given(chunkProcessor.process(1L, issueDate, 10L, pageRequest))
                .willReturn(IssueChunkResult.empty());
        given(issueJobManager.complete(1L, startedAt)).willReturn(issueJob);

        // when
        issueService.resumeIncompleteTodayJob();

        // then
        verify(chunkProcessor).process(1L, issueDate, 10L, pageRequest);
        verify(issueJobManager).complete(1L, startedAt);
    }

    @Test
    void 미완료_job이_없으면_재개하지_않는다() {
        // given
        LocalDate issueDate = LocalDate.of(2026, 4, 27);
        LocalDateTime startedAt = LocalDateTime.of(2026, 4, 27, 7, 0);
        given(issueJobManager.resumeIncomplete(issueDate, startedAt)).willReturn(Optional.empty());

        // when
        issueService.resumeIncompleteTodayJob();

        // then
        verifyNoInteractions(chunkProcessor);
    }

    private Clock clockAt(LocalDateTime localDateTime) {
        return Clock.fixed(localDateTime.atZone(SEOUL).toInstant(), SEOUL);
    }

    private MaeilMailIssueJob issueJob(
            Long id,
            LocalDate issueDate,
            Long lastProcessedTrackId,
            LocalDateTime startedAt
    ) {
        MaeilMailIssueJob issueJob = MaeilMailIssueJob.start(issueDate, 0L, startedAt);
        ReflectionTestUtils.setField(issueJob, "id", id);
        ReflectionTestUtils.setField(issueJob, "lastProcessedTrackId", lastProcessedTrackId);
        return issueJob;
    }
}
